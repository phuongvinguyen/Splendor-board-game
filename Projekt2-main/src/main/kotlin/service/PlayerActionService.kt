package service

import entity.DevelopmentCard
import entity.NobleTile
import entity.Token
import entity.TokenMap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import service.ai.HardAI
import java.io.File
import java.nio.file.Paths

/**
 * Holds a `String` that references the path this program runs in.
 */
private val SAVE_FILE_LOCATION = Paths.get("").toAbsolutePath().toString() + "/save"

/**
 * `.sav`
 *
 * The save file postfix (ending).
 */
private const val SAVE_FILE_POSTFIX = ".sav"

/**
 * `4`
 *
 * The maximum number of allowed save files.
 */
const val MAX_SAVEFILES = 4
val saveFiles: Array<File> = Array(MAX_SAVEFILES) {
    File(SAVE_FILE_LOCATION + (it + 1) + SAVE_FILE_POSTFIX)
}

/**
 * Service layer class that provides methods for implements direct player moves
 */

class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {


    /**
     *  Reserves a development card for the current player
     *
     * Reserves one card for the player, unless he already has three. If reserving is possible, he receives a gold
     * token, if there are Tokens available. Then the card is moved from the source stack to the player's
     * reserved stack.
     *
     * @param source Stack the player chooses a card to reserve from.
     * @param card Card the player wants to reserve.
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     * @throws IllegalArgumentException if a card has been selected that cannot be reserved.
     */
    fun reserveCard(card: DevelopmentCard, source: SourceStack, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        require(currentPlayer.reservedCards.size < 3) { "A maximum of three reservations are possible." }

        // Remove gold token (joker), if still available, and give it to the player
        val tokens = game.tokens.toMutableTokenMap()
        val playerTokens = currentPlayer.tokens.toMutableTokenMap()


        if (tokens[Token.GOLD] > 0) {
            tokens[Token.GOLD] -= 1
            playerTokens[Token.GOLD] += 1

            game.tokens = tokens
            currentPlayer.tokens = playerTokens
        }
        // Remove the card to be reserved from the source stack
        if (source == SourceStack.RESERVED) throw IllegalArgumentException("Invalid stack.")

        removeCardFromStack(card, source)

        // Add the card to players reservedCards stack
        currentPlayer.reservedCards += card

        if (doGuiStuff) {
            // Remove the card from the source stack
            rootService.currentGame.gameHistory.add(
                "${currentPlayer.name} has reserved a development card that" +
                        " ${card.easierReadableString()}"
            )
            onAllRefreshables { refreshAfterCardReserved(card, source) }
        }
    }

    /**
     * Buys a card that the current player has selected
     *
     * Buys the selected card for the player, provided he has enough tokens (including bonuses).
     * After success the card is moved from the source stack to the player's reserved stack. Tokens used for payment
     * will be taken from the player and added to the game.
     *
     * @param card Card the player wants to buy.
     * @param source Stack the player chooses a card to reserve from.
     * @param payment Map of tokens and their quantity with which the player chooses for payment
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     * @throws IllegalArgumentException if the selected card can not be purchased.
     */
    fun buyCard(card: DevelopmentCard, payment: TokenMap, source: SourceStack, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        val playerTokens = currentPlayer.tokens.toMutableTokenMap()
        val gameTokens = game.tokens.toMutableTokenMap()

        val paymentIncludingBonus = payment.toMutableTokenMap()

        // Checks if the player owns the selected tokens
        check(payment.all { (token, amt) ->
            currentPlayer.tokens[token] >= amt
        }) { "Player does not have enough tokens." }

        // Adds the bonuses
        for (developmentCard in currentPlayer.cards) {
            if (developmentCard.bonus != null) {
                paymentIncludingBonus[developmentCard.bonus] += 1
            }
        }

        check(card.cost.all { (token, amt) ->
            // use gold if the player did not pay enough
            val diff = amt - paymentIncludingBonus[token]
            if (diff <= 0) {
                return@all true
            }
            val playerGold = paymentIncludingBonus[Token.GOLD]
            if (playerGold >= diff) {
                paymentIncludingBonus[Token.GOLD] = playerGold - diff
                true
            } else {
                false
            }
        }) { "Not enough tokens paid." }


        // subtract tokens in playerStack and add it to game stack.
        for ((token, amt) in payment) {
            playerTokens[token] -= amt
            gameTokens[token] += amt
        }
        currentPlayer.tokens = playerTokens
        game.tokens = gameTokens

        currentPlayer.cards += card

        // Remove the purchased card from the source stack
        if (source.isDraw()) throw IllegalArgumentException("Invalid stack.")
        removeCardFromStack(card, source)

        if (doGuiStuff) {
            rootService.currentGame.gameHistory.add(
                "${currentPlayer.name} " +
                        "has bought a development card that ${card.easierReadableString()}"
            )
            onAllRefreshables { refreshAfterCardBought(card, source) }
        }
    }

    /**
     * Implementation of the player selecting tokens.
     *
     * The different ways of selecting tokens are reviewed. Several options need to be checked:
     * If the player has selected only one token, one token should be selectable at all. If two tokens of the same type
     * are selected, only two should be selectable at all. If there are two tokens of the same type, the number of
     * remaining tokens of this type must be at least 4. If these two tokens are not equal, only two may be available
     * in total. When selecting three tokens, they must be of different types.
     * If the selection made by the player is possible, the tokens are given to the player by the game.
     *
     * @param tokens Tokens the player selects to receive them
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     */
    fun selectTokens(tokens: List<Token>, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()

        require(!tokens.contains(Token.GOLD)) { "Unerlaubter Zug" }

        val tokensWithoutGold = game.tokens.toMutableTokenMap().apply { this[Token.GOLD] = 0 }
        if (tokens.size == 1) {
            require(tokens.all { tokensWithoutGold[it] > 0 }) { "Not enough tokens available." }
        } else {
            require(tokens.size in 2..3) { "Selection not possible. " }

            if (tokens.size == 2) {
                if (tokens[0] != tokens[1]) {
                    require(tokens.all { tokensWithoutGold[it] > 0 }) { "Not enough tokens available." }
                } else {
                    require(tokens[0] == tokens[1]) { "Selection not possible." }
                    require(game.tokens[tokens[0]] >= 4) { "Selection not possible. " }
                }

            }
            if (tokens.size == 3) {
                require(tokens.distinct().size == 3)
                tokens.forEach { require(game.tokens[it] > 0) { "Selection not possible." } }
            }
        }
        // transfers the Tokens from game to player
        val gameTokenCopy = game.tokens.toMutableTokenMap()
        val playerTokenCopy = currentPlayer.tokens.toMutableTokenMap()
        tokens.forEach {
            gameTokenCopy[it] -= 1
            playerTokenCopy[it] += 1
        }
        game.tokens = gameTokenCopy
        currentPlayer.tokens = playerTokenCopy

        if (doGuiStuff) {
            rootService.currentGame.gameHistory.add("${currentPlayer.name} has selected ${tokens.joinToString()} ")
            onAllRefreshables { refreshAfterTokenDrawn() }
        }
    }

    /**
     * This method is used when a player wants to return some tokens, after they had more than 10.
     *
     * @param tokens The tokens the player wants to return
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     * @throws IllegalStateException if the player does not have all the tokens or the number of tokens is below 10
     */
    fun returnTokens(tokens: List<Token>, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val player = rootService.gameService.getCurrentPlayer()

        val gameTokens = game.tokens.toMutableTokenMap()
        val playerTokens = player.tokens.toMutableTokenMap()

        for (token in tokens) {
            check(player.tokens[token] > 0) { "Spieler hat nicht genug Tokens" }
            playerTokens[token] -= 1
            gameTokens[token] += 1
        }

        player.tokens = playerTokens
        game.tokens = gameTokens

        if (doGuiStuff) {
            check(player.totalTokens() <= 10) { "Nicht genug Tokens zurückgegeben" }
            rootService.currentGame.gameHistory.add("${player.name} has returned ${tokens.joinToString()} ")
        }
    }

    /**
     * If more than noble can visit the player, the player can choose the noble.
     *
     * @param nobleTile The selected noble
     * @param doGuiStuff Whether to do stuff that is only required to happen in the gui...
     */
    fun selectNoble(nobleTile: NobleTile, doGuiStuff: Boolean = true) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()

        require(nobleTile in game.nobleTiles) { "Noble card not available." }
        game.nobleTiles -= nobleTile
        currentPlayer.nobleTiles += nobleTile

        if (doGuiStuff) {
            rootService.currentGame.gameHistory.add(
                "${currentPlayer.name} has selected a noble tile that " +
                        "${nobleTile.easierReadableString()} "
            )
            onAllRefreshables { refreshAfterNobleVisit(nobleTile) }
        }
    }

    /**
     * Returns the best move the hard AI finds as a String
     *
     * If a player asks for a tip, they are no longer highscore worthy.
     *
     * @return the string representation of the best move
     */
    fun getHint(): String {
        val move = HardAI().determineBestMove(rootService.currentGame.getCurrentGame(), 1000)
        rootService.gameService.getCurrentPlayer().allowHighscore = false
        rootService.currentGame.gameHistory.add("${rootService.gameService.getCurrentPlayer().name} has asked for a hint")
        return move.toString()
    }

    /**
     * redo resets a move by accessing the previous GameManager.
     *
     * @throws IllegalStateException if no previous move was found
     */
    fun redo() {
        val nextGameIndex = rootService.currentGame.currentGameIndex + 1
        check(nextGameIndex < rootService.currentGame.gameStates.size) { "There is no next round" }
        rootService.currentGame.gameHistory.add(
            "${rootService.gameService.getCurrentPlayer().name} " +
                    "has reversed the undo"
        )
        rootService.currentGame.currentGameIndex = nextGameIndex
        rootService.currentGame.currentState = rootService.currentGame.gameStates[nextGameIndex].copy()
        onAllRefreshables { refreshAfterTurn(TurnType.REDO) }
    }


    /**
     * undo redoes a move by accessing the next GameManager.
     *
     * @throws IllegalStateException if no following move was found
     */
    fun undo() {
        val game = rootService.currentGame
        val prevGameIndex = game.currentGameIndex - 1
        check(prevGameIndex >= 0) { "There is no previous round." }
        game.gameHistory.add("${rootService.gameService.getCurrentPlayer().name} has undone the last turn")
        game.currentGameIndex = prevGameIndex
        game.currentState = game.gameStates[prevGameIndex].copy()
        game.allowHighscore = false
        onAllRefreshables { refreshAfterTurn(TurnType.UNDO) }
    }

    /**
     * Saves the currently active game state (including previous
     * and future game states if they exist) in `saveI.sav` where
     * `I` may be an index from 1 to (and including) 4. Calling
     * this method with `2` for [saveGameIndex] saves the
     * associated JSON file `save2.sav`. Currently, if there
     * already exists a file with that name, it will be
     * overwritten regardless of its contents.
     * @param saveGameIndex the index of the file that
     * should be written to disk (can be any number from 1
     * to 4)
     */
    fun saveGame(saveGameIndex: Int) {
        val dataToWrite = Json.encodeToString(rootService.currentGame)
        val shouldWrite = true
        if (saveFiles[saveGameIndex - 1].exists()) { // falls die Datei bereits existiert
            // Spieler fragen, ob überschreiben werden soll? Dann shouldWrite entsprechend setzen.
            /* ... */
        }
        if (shouldWrite) {
            saveFiles[saveGameIndex - 1].writeText(dataToWrite)
            rootService.currentGame.gameHistory.add(
                "${rootService.gameService.getCurrentPlayer().name} " +
                        "has saved the game to slot $saveGameIndex"
            )
        }
    }

    /**
     * Loads a save game from the JSON file `saveI.sav` where
     * `I` may be an index from 1 to (and including) 4. Previous
     * and future game states (if they exist) are also restored.
     * Calling this method with `2` for [loadGameIndex] loads the
     * associated JSON file `save2.sav`. The currently active
     * game (if there is one) will be overwritten.
     * If there is no file in the expected format the method
     * will return without a result.
     * @param loadGameIndex the index of the saved game that
     * should be loaded from disk (can be any number from 1
     * to 4)
     */
    fun loadGame(loadGameIndex: Int) {
        if (saveFiles[loadGameIndex - 1].exists()) {
            rootService.currentGame = Json.decodeFromString(saveFiles[loadGameIndex - 1].readText())
            rootService.currentGame.gameHistory
                .add("A Game with ${rootService.currentGame.getCurrentGame()
                    .players.joinToString { it.name }} was loaded")
            onAllRefreshables { refreshAfterTurn(TurnType.NORMAL) }
        }
    }

    /**
     * This helper method removes the specified card from its stack
     *
     * @param card The Card to remove
     * @param source The stack the card is at
     *
     * @throws IllegalArgumentException If the card is not contained in the stack
     */
    private fun removeCardFromStack(card: DevelopmentCard, source: SourceStack) {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()

        if (source == SourceStack.RESERVED) {
            val reservedCards = currentPlayer.reservedCards.toMutableList()
            require(reservedCards.remove(card)) { "Given card not found." }
            currentPlayer.reservedCards = reservedCards
        } else if (source.isOpen()) {
            val openCards = game.openCards.toMutableList()
            val index = source.getIndex()
            openCards[index] =
                openCards[index].toMutableList().apply { require(remove(card)) { "Given card not found." } }
            game.openCards = openCards
        } else if (source.isDraw()) {
            val drawCards = game.drawCards.toMutableList()
            val index = source.getIndex()
            drawCards[index] = drawCards[index].toMutableList().apply {
                require(last() == card) { "Improper card selected." }
                removeLast()
            }
            game.drawCards = drawCards
        }
    }
}