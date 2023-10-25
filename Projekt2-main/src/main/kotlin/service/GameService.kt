package service

import entity.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random


/**
 * Service layer class that provides methods for game control that do not represent a direct player move.
 */
class GameService(internal val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Returns the player who follows the player who is currently on his turn
     *
     * Accesses the list of players in currentGame and increases the player index by one if it is not the last
     * player on the list. If the last player is currently on the turn, then the next player is the first and
     * therefore in the player list at index 0.
     *
     * @return Player following the current one
     */
    fun getNextPlayer(): Player {
        val game = rootService.currentGame.getCurrentGame()
        return if (game.currentPlayer < game.players.size - 1) {
            game.players[game.currentPlayer + 1]
        } else {
            game.players[0]
        }
    }

    /**
     * Player currently on the turn
     *
     * Accesses the list of current players and outputs the current player using his index
     *
     * @return Player currently on the turn
     */
    fun getCurrentPlayer(): Player {
        val game = rootService.currentGame.getCurrentGame()
        return game.players[game.currentPlayer]
    }

    /**
     *  The checkGameEnd() method checks whether the game is over or the last game round has started.
     *
     *  Checks by the Boolean isLastRound whether the last round has already been started by a win condition.
     *  If it is the first player's turn, the game would be over in this case.
     *
     *  @return A Boolean is returned; if this is true, then the end of the game has been reached. If false,
     *  the game is not  yet over yet.
     */
    fun checkGameEnd(): Boolean {
        val game = rootService.currentGame.getCurrentGame()

        if (game.passingCounter == game.players.size) {
            return true
        }

        if ((game.currentPlayer + 1) % game.players.size == 0 && game.isLastRound) {
            return true
        }
        return false

    }


    /**
     *  The startNewGame() method starts a new game
     *
     *  The new game will be started with a list of players passed in parameter [playerList].
     *  Then the tokens and cards are distributed in the private method [dealCardsAndTokens].
     *
     *  @param playerList the list of players (or bots) that participate in the game
     *  @exception IllegalArgumentException If too few or too many players have been handed over
     */
    fun startNewGame(playerList: List<Player>, selectedFiles: List<File>) {
        require(playerList.size in 2..4) { "Nicht die passende Anzahl an Spieler" }
        rootService.currentGame = GameManager(GameState())
        rootService.currentGame.getCurrentGame().players = playerList
        var nobleCardsStream: InputStream? = this.javaClass.getResourceAsStream("/splendor-adligenkarten.csv")
        var developmentCardsStream: InputStream? =
            this.javaClass.getResourceAsStream("/splendor-entwicklungskarten.csv")

        if (selectedFiles.size >= 2) {
            var filesCollected = 0
            for (selectedFile in selectedFiles) {
                if (selectedFile.nameWithoutExtension.contains("entwicklungskarten", true)) {
                    developmentCardsStream = FileInputStream(selectedFile)
                    filesCollected += 1
                }
                if (selectedFile.nameWithoutExtension.contains("adligenkarten", true)) {
                    nobleCardsStream = FileInputStream(selectedFile)
                    filesCollected += 1
                }
            }
            check(filesCollected == 2) { "Falsche CSV Dateien" }
        }

        // Distribute cards and tokens
        check(developmentCardsStream != null && nobleCardsStream != null)
        dealCardsAndTokens(
            loadDevelopmentCards(developmentCardsStream),
            loadNobleTiles(nobleCardsStream),
            playerList.size,
            !selectedFiles.isNotEmpty()
        )

        rootService.currentGame = GameManager(rootService.currentGame.getCurrentGame())
        rootService.currentGame.gameHistory.add("A Game was started with: ${playerList.joinToString { it.name }}")
    }

    /**
     *  The calculatePoints() method calculates the current prestige points for a players
     *
     *  @param player Player whose score is to be calculated.
     *  @return Returns the score as Int
     */
    fun calculatePoints(player: Player): Int {
        var calculatedPoints = 0
        player.cards.forEach { calculatedPoints += it.prestigePoints }
        // Each noble tile is worth 3 points
        calculatedPoints += (player.nobleTiles.size * 3)
        return calculatedPoints
    }


    /**
     * a function that returns the index of the player with the most points
     */
    fun getBestPlayerIndex(): Int {
        val game = rootService.currentGame.getCurrentGame()
        return game.players.indices.maxOfWith(compareBy(
            { calculatePoints(game.players[it]) },
            { -game.players[it].cards.size }
        )) { it }
    }

    /**
     * Method that checks and sets the conditions to start the next player move.
     *
     * Checks if the player has more than 10 tokens and has to give some away. It also checks whether a noble must be
     * selected by the player. If neither of these applies or has been resolved, the next move is prepared and the
     * current one is saved. After a completed turn a new previousRound and nextRound will be set.
     */
    fun startNextRound() = yielder<Unit> { yielder ->
        val currentPlayer = getCurrentPlayer()
        if (currentPlayer.totalTokens() > 10) {
            onAllRefreshables { refreshBeforeTokenDiscard() }
            yield(Unit)
        }

        val availableNobles = getAvailableNobles()
        val selectedNoble = when (availableNobles.size) {
            0 -> null
            1 -> availableNobles[0]
            else -> {
                onAllRefreshables { refreshBeforeNobleVisit(availableNobles) }
                yield(Unit)
                yielder.argument as NobleTile
            }
        }
        startNextRoundInner(selectedNoble)
    }

    /**
     * Starts the next round, but does not collect any input anymore.
     * @param selectedNoble The noble tile that should get added to the player
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     */
    fun startNextRoundInner(selectedNoble: NobleTile?, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()

        check(currentPlayer.totalTokens() <= 10) { "Player may have at most 10 tokens" }

        if (!rootService.checkPlayerMove.checkIfMovePossible()) {
            game.passingCounter++
            rootService.currentGame.gameHistory.add(
                "${currentPlayer.name}'s " +
                        "turn was skipped because no action was possible"
            )
        } else {
            game.passingCounter = 0
        }

        if (selectedNoble != null) {
            rootService.playerActionService.selectNoble(selectedNoble, doGuiStuff)
        }

        // Fill in gaps of cards
        for (i in 0..2) {
            if ((game.openCards.getOrNull(i)?.size ?: 0) < 4) {
                if (game.drawCards[i].isNotEmpty()) {
                    val tierDrawCards = game.drawCards[i].toMutableList()
                    val tierOpenCards = game.openCards[i].toMutableList()

                    tierOpenCards.add(tierDrawCards.removeLast())
                    game.openCards = game.openCards.toMutableList().apply { set(i, tierOpenCards) }
                    game.drawCards = game.drawCards.toMutableList().apply { set(i, tierDrawCards) }

                }
            }
        }

        // Checks if the game is already over
        if (checkGameEnd()) {
            if (game.passingCounter == game.players.size) {
                onAllRefreshables { refreshAfterGameFinished(0) }
            } else {
                onAllRefreshables { refreshAfterGameFinished(1) }
            }
            return
        }

        if (calculatePoints(getCurrentPlayer()) >= 15) {
            game.isLastRound = true
        }

        // Set next player as current player
        if (game.currentPlayer < game.players.size - 1) {
            game.currentPlayer++
        } else {
            game.currentPlayer = 0
        }

        rootService.currentGame.addRound()
        onAllRefreshables { refreshAfterTurn(TurnType.NORMAL) }
    }

    /**
     * @return A list of nobles that can visit the player
     */
    fun getAvailableNobles() = rootService.currentGame.getCurrentGame().nobleTiles.filter { noble ->
        noble.requirements.all { (token, amount) ->
            getCurrentPlayer().cards.count { card ->
                card.bonus == token
            } >= amount
        }
    }

    /**
     * Loads the highscore stored as JSON
     *
     * @return the a [HighscoreList], which was decoded from the JSON
     */
    fun loadHighScoreList(): HighscoreList {
        val file = File("highscore.json")
        if (!file.exists()) return HighscoreList()
        if (!file.canRead()) throw IOException("Cannot read file")
        val json = file.readText()
        if (json.isEmpty()) return HighscoreList()

        return Json.decodeFromString(serializer(), json)
    }

    /**
     * Inserts a new highscore entry into a JSON-File
     *
     */
    fun saveHighScore() {
        //Check worthy
        require(rootService.currentGame.allowHighscore) { "Game not allowed to save" }

        val scoreboardEntries = mutableListOf<ScoreboardEntry>()
        //Set Score in game
        for (player in rootService.currentGame.getCurrentGame().players) {
            if (player.allowHighscore) {
                scoreboardEntries.add(
                    ScoreboardEntry(
                        name = player.name,
                        score = calculatePoints(player),
                        developmentCards = player.cards.size,
                        numRounds = rootService.currentGame.gameStates.size
                    )
                )
            }
        }

        require(scoreboardEntries.isNotEmpty()) { "No Player is allowed to save Highscore" }
        val highscoreList = loadHighScoreList()
        highscoreList.scores.addAll(scoreboardEntries)
        highscoreList.scores.sortDescending()

        while (highscoreList.scores.size >= 6) {
            highscoreList.scores.removeLast()
        }
        val json = Json.encodeToString(serializer(), highscoreList)

        File("highscore.json").writeText(json)

    }

    /**
     * Private method: Distributes the cards and tokens to the stacks and piles
     */
    private fun dealCardsAndTokens(
        developmentCards: List<DevelopmentCard>, nobleTiles: List<NobleTile>, numberOfPlayers: Int, shuffle: Boolean
    ) {
        val game = rootService.currentGame.getCurrentGame()

        // The development cards are sorted and shuffled according to their level
        val allTierDevelopmentCards = mutableListOf<MutableList<DevelopmentCard>>()
        for (tier in 1..3) {
            allTierDevelopmentCards.add(
                developmentCards.filter { it.tier == tier }.toMutableList()
                    .apply { if (shuffle) shuffle() else reverse() })
        }

        // Cards are dealt for the different levels of development cards
        val openCardsList = mutableListOf<List<DevelopmentCard>>()
        allTierDevelopmentCards.forEach {
            openCardsList.add(it.takeLast(4))
            it.removeAll(it.takeLast(4))
        }

        // According to the rules, a different number of tokens is added to the game depending on the number of players.
        val minusTokens = when (numberOfPlayers) {
            2 -> 3
            3 -> 2
            4 -> 0
            else -> throw IllegalArgumentException("Falsche Spielerzahl angegeben")
        }

        val tokensMap = MutableTokenMap()
        val allTokensExceptGold = Token.values().filter { it != Token.GOLD }
        for (token in allTokensExceptGold) {
            tokensMap[token] = 7 - minusTokens
        }
        tokensMap[Token.GOLD] = 5

        game.tokens = tokensMap

        game.drawCards = allTierDevelopmentCards

        game.openCards = openCardsList

        // The noble cards are placed. The number of Noble cards is equal to the number of players + 1.
        val nobleTilesShuffled = if (shuffle) {
            nobleTiles.shuffled()
        } else {
            nobleTiles.reversed()
        }
        game.nobleTiles = nobleTilesShuffled.take(numberOfPlayers + 1)
    }
}

/**
 * Private function that reads a CSV file with the development cards puts it into a mutableList .
 */
private fun loadDevelopmentCards(developmentCardsFile: InputStream?): List<DevelopmentCard> {
    check(developmentCardsFile != null) { "DevelopmentCard Datei fehlt" }
    val developmentCards = mutableListOf<DevelopmentCard>()
    val csvParser = CSVParser.parse(
        developmentCardsFile,
        charset("UTF-8"),
        CSVFormat.Builder.create(CSVFormat.DEFAULT).setTrim(true).setSkipHeaderRecord(true).setHeader().build()
    )
    val requirementsContained = Token.values().filter { csvParser.headerNames.contains(it.translation) }
    check(
        requirementsContained.size + 4 ==
                csvParser.headerNames.size && csvParser.headerNames.size == 9
    ) { "Unbekanntes Format" }
    val random = Random(42)
    for (csvRecord in csvParser) {
        val imageID = (0..14).random(random)
        val requirements = requirementsContained.associateWith { csvRecord.get(it.translation).toInt() }
        val prestigePoints = csvRecord.get("PRESTIGEPUNKTE").toInt()
        val tier = csvRecord.get("STUFE").toInt()
        val bonus = Token.values().find { it.translation.lowercase() == csvRecord.get("BONUS") }
            ?: throw IllegalArgumentException("Bonus existiert nicht")
        developmentCards.add(DevelopmentCard(requirements.toTokenMap(), bonus, prestigePoints, tier, imageID))
    }
    return developmentCards
}

/**
 * Private function that reads a CSV file with the noble cards and puts it into a mutableList .
 */
private fun loadNobleTiles(nobleFile: InputStream?): List<NobleTile> {
    check(nobleFile != null) { "NobleTile Datei fehlt" }
    val nobleTiles = mutableListOf<NobleTile>()
    val csvParser = CSVParser.parse(
        nobleFile,
        charset("UTF-8"),
        CSVFormat.Builder.create(CSVFormat.DEFAULT).setTrim(true).setSkipHeaderRecord(true).setHeader().build()
    )
    val requirementsContained = Token.values().filter { csvParser.headerNames.contains(it.translation) }
    check(
        requirementsContained.size + 2 ==
                csvParser.headerNames.size && csvParser.headerNames.size == 7
    ) { "Unbekanntes Format" }
    for (csvRecord in csvParser) {
        val imageID = csvRecord.get("ID").toInt() - 90
        val requirements = requirementsContained.associateWith { csvRecord.get(it.translation).toInt() }
        val prestigePoints = csvRecord.get("PRESTIGEPUNKTE").toInt()
        nobleTiles += NobleTile(requirements.toTokenMap(), imageID, prestigePoints)
    }
    return nobleTiles
}