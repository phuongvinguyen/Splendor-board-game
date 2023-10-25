package service

import entity.*
import view.Refreshable
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


/**
 * Test class for [GameService]
 */
internal class GameServiceTest {
    /**
     * The startNewGame function is tested with two players
     */
    @Test
    fun testStartNewGameTwoPlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame(
            listOf(
                Player("Test1", PlayerType.HUMAN),
                Player("Test2", PlayerType.HUMAN)
            ),
            listOf()
        )
        val game = rootService.currentGame.getCurrentGame()

        // Are the players set up correctly?
        assertEquals(2, game.players.size)
        assertEquals("Test1", game.players[0].name)
        assertEquals("Test2", game.players[1].name)
        assertEquals(PlayerType.HUMAN, game.players[0].playerType)
        assertEquals(PlayerType.HUMAN, game.players[1].playerType)
        // Are the cards set up correctly?
        assertEquals(4, game.openCards[0].size)
        assertEquals(4, game.openCards[1].size)
        assertEquals(4, game.openCards[2].size)
        assertEquals(36, game.drawCards[0].size)
        assertEquals(26, game.drawCards[1].size)
        assertEquals(16, game.drawCards[2].size)

        assertEquals(3, game.nobleTiles.size)
        // Are the tokens set up correctly?
        assertEquals(4, game.tokens[Token.EMERALD])
        assertEquals(4, game.tokens[Token.DIAMOND])
        assertEquals(4, game.tokens[Token.SAPPHIRE])
        assertEquals(4, game.tokens[Token.ONYX])
        assertEquals(4, game.tokens[Token.RUBY])
        assertEquals(5, game.tokens[Token.GOLD])
    }

    /**
     * The startNewGame function is tested with three players
     */
    @Test
    fun testStartNewGameThreePlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame(
            listOf(
                Player("Test1", PlayerType.HUMAN),
                Player("Test2", PlayerType.HUMAN),
                Player("Test3", PlayerType.HUMAN)
            ),
            listOf()
        )
        val game = rootService.currentGame.getCurrentGame()
        // Are the players set up correctly?
        assertEquals(3, game.players.size)
        assertEquals("Test1", game.players[0].name)
        assertEquals("Test2", game.players[1].name)
        assertEquals("Test3", game.players[2].name)
        assertEquals(PlayerType.HUMAN, game.players[0].playerType)
        assertEquals(PlayerType.HUMAN, game.players[1].playerType)
        assertEquals(PlayerType.HUMAN, game.players[2].playerType)
        // Are the cards set up correctly?
        assertEquals(4, game.openCards[0].size)
        assertEquals(4, game.openCards[1].size)
        assertEquals(4, game.openCards[2].size)
        assertEquals(36, game.drawCards[0].size)
        assertEquals(26, game.drawCards[1].size)
        assertEquals(16, game.drawCards[2].size)

        assertEquals(4, game.nobleTiles.size)
        // Are the tokens set up correctly?
        assertEquals(5, game.tokens[Token.EMERALD])
        assertEquals(5, game.tokens[Token.DIAMOND])
        assertEquals(5, game.tokens[Token.SAPPHIRE])
        assertEquals(5, game.tokens[Token.ONYX])
        assertEquals(5, game.tokens[Token.RUBY])
        assertEquals(5, game.tokens[Token.GOLD])
    }

    /**
     * The startNewGame function is tested with four players
     */
    @Test
    fun testStartNewGameFourPlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame(
            listOf(
                Player("Test1", PlayerType.HUMAN),
                Player("Test2", PlayerType.HUMAN),
                Player("Test3", PlayerType.HUMAN),
                Player("Test4", PlayerType.HUMAN)
            ),
            listOf()
        )
        val game = rootService.currentGame.getCurrentGame()
        // Are the players set up correctly?
        assertEquals(4, game.players.size)
        assertEquals("Test1", game.players[0].name)
        assertEquals("Test2", game.players[1].name)
        assertEquals("Test3", game.players[2].name)
        assertEquals("Test4", game.players[3].name)
        assertEquals(PlayerType.HUMAN, game.players[0].playerType)
        assertEquals(PlayerType.HUMAN, game.players[1].playerType)
        assertEquals(PlayerType.HUMAN, game.players[2].playerType)
        assertEquals(PlayerType.HUMAN, game.players[3].playerType)
        // Are the cards set up correctly?
        assertEquals(4, game.openCards[0].size)
        assertEquals(4, game.openCards[1].size)
        assertEquals(4, game.openCards[2].size)
        assertEquals(36, game.drawCards[0].size)
        assertEquals(26, game.drawCards[1].size)
        assertEquals(16, game.drawCards[2].size)

        assertEquals(5, game.nobleTiles.size)
        // Are the tokens set up correctly?
        assertEquals(7, game.tokens[Token.EMERALD])
        assertEquals(7, game.tokens[Token.DIAMOND])
        assertEquals(7, game.tokens[Token.SAPPHIRE])
        assertEquals(7, game.tokens[Token.ONYX])
        assertEquals(7, game.tokens[Token.RUBY])
        assertEquals(5, game.tokens[Token.GOLD])
    }

    /**
     * The startNewGame function is tested with one player and five players
     */
    @Test
    fun testStartNewGameWrongPlayerCount() {
        val rootService = RootService()
        //is it impossible to start the game with one player?
        assertFails {
            rootService.gameService.startNewGame(
                listOf(
                    Player("Test1", PlayerType.HUMAN),
                ),
                listOf()
            )
        }
        //is it impossible to start the game with five players?
        assertFails {
            rootService.gameService.startNewGame(
                listOf(
                    Player("Test1", PlayerType.HUMAN),
                    Player("Test2", PlayerType.HUMAN),
                    Player("Test3", PlayerType.HUMAN),
                    Player("Test4", PlayerType.HUMAN),
                    Player("Test5", PlayerType.HUMAN)
                ),
                listOf()
            )
        }
    }

    /**
     * The getCurrentPlayer and getNextFunctions functions are tested
     */
    @Test
    fun testCurrentAndNextPlayer() {
        val rootService = RootService()
        val service = GameService(rootService)
        val player1 = Player("Test1", PlayerType.HUMAN)
        val player2 = Player("Test2", PlayerType.HUMAN)
        service.startNewGame(listOf(player1, player2), listOf())
        assertEquals(player1, service.getCurrentPlayer())
        assertEquals(player2, service.getNextPlayer())
        rootService.currentGame.getCurrentGame().currentPlayer++
        assertEquals(player1, service.getNextPlayer())
    }

    /**
     * The calculatePoints function is tested
     */
    @Test
    fun testCalculatePoints() {
        val service = GameService(RootService())
        val player1 = Player("Test1", PlayerType.HUMAN).apply {
            this.cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 2, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 10, 3, 0)
            )
        }
        val player2 = Player("Test2", PlayerType.HUMAN).apply {
            this.cards = listOf(DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 2, 3, 0))
            this.nobleTiles = listOf(NobleTile(MutableTokenMap(), 0, 0), NobleTile(MutableTokenMap(), 0, 0))
        }
        service.startNewGame(listOf(player1, player2), listOf())
        assertEquals(12, service.calculatePoints(service.getCurrentPlayer()))
        assertEquals(8, service.calculatePoints(service.getNextPlayer()))
    }

    /**
     * The checkGameEnd function is tested
     */
    @Test
    fun testCheckGameEnd() {
        val rootService = RootService()
        val service = GameService(rootService)
        val player1 = Player("Test1", PlayerType.HUMAN)
        val player2 = Player("Test2", PlayerType.HUMAN).apply {
            this.cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 5, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 10, 3, 0)
            )
        }
        val player3 = Player("Test3", PlayerType.HUMAN)
        val player4 = Player("Test4", PlayerType.HUMAN)
        service.startNewGame(listOf(player1, player2, player3, player4), listOf())
        // checkGameEnd should be false because player 1 has < 13 prestige points and the last round has not started
        assertEquals(false, service.checkGameEnd())
        rootService.gameService.startNextRoundInner(null)
        //checkGameEnd should be false because the last round is not finished yet
        assertEquals(false, service.checkGameEnd())
        rootService.gameService.startNextRoundInner(null)
        //checkGameEnd should be false because the last round is not finished yet
        assertEquals(false, service.checkGameEnd())
        rootService.gameService.startNextRoundInner(null)
        //checkGameEnd should be true because player 2 has > 13 prestige points and the last round is finished
        assertEquals(true, service.checkGameEnd())
    }

    /**
     * The saveHighScore and loadHighScoreList functions are tested
     */
    @Test
    fun testSaveAndLoadHighScoreList() {
        val rootService = RootService()
        val service = GameService(rootService)
        val player1 = Player(
            "Test1", PlayerType.HUMAN, cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 5, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 10, 3, 0)
            )
        )
        val player2 = Player(
            "Test2",
            PlayerType.HUMAN,
            cards = listOf(DevelopmentCard(MutableTokenMap(), Token.ONYX, 13, 3, 0)),
            nobleTiles = listOf(
                NobleTile(MutableTokenMap(), 0, 3)
            )
        )
        val higScoreFile = File("highscore.json")
        higScoreFile.delete()
        service.startNewGame(listOf(player1, player2), listOf())
        service.saveHighScore()
        val highScoreList = service.loadHighScoreList()
        // The loaded ScoreboardEntries should have the same content as the previously saved ScoreboardEntries
        assertEquals("Test1", highScoreList.scores[1].name)
        assertEquals(15, highScoreList.scores[1].score)
        assertEquals(2, highScoreList.scores[1].developmentCards)
        assertEquals(1, highScoreList.scores[1].numRounds)

        assertEquals("Test2", highScoreList.scores[0].name)
        assertEquals(16, highScoreList.scores[0].score)
        assertEquals(1, highScoreList.scores[0].developmentCards)
        assertEquals(1, highScoreList.scores[0].numRounds)
        higScoreFile.delete()
    }


    /**
     * The nextRound function is tested
     */
    @Test
    fun testNextRound() {
        val rootService = RootService()
        val service = GameService(rootService)
        var yielder: Yielder<Unit>?
        val player1 = Player("Test1", PlayerType.HUMAN).apply {
            this.cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 4, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 6, 3, 0)
            )
        }
        val player2 = Player("Test2", PlayerType.HUMAN).apply {
            this.cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 8, 3, 0)
            )
        }
        val player3 = Player("Test3", PlayerType.HUMAN).apply {
            this.cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 15, 3, 0)
            )
        }
        val player4 = Player("Test4", PlayerType.HUMAN)
        val testRefreshable = TestRefreshable()
        service.addRefreshable(testRefreshable)
        rootService.playerActionService.addRefreshable(testRefreshable)
        service.startNewGame(listOf(player1, player2, player3, player4), listOf())
        val nobleTile1 = NobleTile(mapOf(Token.DIAMOND to 1).toTokenMap(), 0, 2)
        val nobleTile2 = NobleTile(mapOf(Token.SAPPHIRE to 1).toTokenMap(), 0, 2)
        rootService.currentGame.getCurrentGame().nobleTiles = listOf(nobleTile1, nobleTile2)
        yielder = service.startNextRound()
        yielder.continueWith(nobleTile2)
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(true, testRefreshable.accessedBeforeNobleVisit)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(true, testRefreshable.accessedAfterNobleVisit)
        removeOneCardFromTierOne(rootService)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(4, rootService.currentGame.getCurrentGame().openCards[0].size)
        val newDrawCards = rootService.currentGame.getCurrentGame().drawCards.toMutableList()
        newDrawCards[0] = mutableListOf()
        rootService.currentGame.getCurrentGame().drawCards = newDrawCards
        removeOneCardFromTierOne(rootService)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(true, testRefreshable.accessedAfterGameFinished)
    }

    private fun removeOneCardFromTierOne(rootService: RootService) {
        val newOpenCards = rootService.currentGame.getCurrentGame().openCards.toMutableList()
        val newOpenCardsTierOne = newOpenCards[0].toMutableList().apply { removeFirst() }
        newOpenCards[0] = newOpenCardsTierOne
        rootService.currentGame.getCurrentGame().openCards = newOpenCards
    }

    /**
     * The token discard in the nextRound function is tested
     */
    @Test
    fun testNextRoundTokenReduction() {
        class DummyRefreshable : Refreshable {
            val service = GameService(RootService())
            var yielder: Yielder<Unit>? = null

            init {
                service.startNewGame(
                    listOf(
                        Player("A", PlayerType.HUMAN, tokens = mapOf(Token.RUBY to 11).toTokenMap()),
                        Player("B", PlayerType.EASY)
                    ),
                    listOf()
                )
                service.addRefreshable(this)
                yielder = service.startNextRound()
                while (yielder!!.hasNext()) {
                    yielder!!.continueWith(null)
                }

                assertEquals(10, service.rootService.currentGame.getCurrentGame().players[0].tokens[Token.RUBY])
            }

            override fun refreshAfterTurn(turnType: TurnType) {
                assert(!yielder!!.hasNext())
                yielder = null
            }

            override fun refreshBeforeTokenDiscard() {
                val tokens = service.getCurrentPlayer().tokens.toMutableTokenMap()
                tokens[Token.RUBY] = 10
                service.getCurrentPlayer().tokens = tokens
            }

        }

        DummyRefreshable()
    }

    /**
     * Tests if the game handles a deadlock correctly
     */
    @Test
    fun testPassing() {
        val rootService = RootService()
        val service = GameService(rootService)
        var yielder: Yielder<Unit>?
        val player1 = Player("Test1", PlayerType.HUMAN)
        val player2 = Player("Test2", PlayerType.HUMAN)
        val player3 = Player("Test3", PlayerType.HUMAN)
        val player4 = Player("Test4", PlayerType.HUMAN)
        val testRefreshable = TestRefreshable()
        service.addRefreshable(testRefreshable)
        rootService.playerActionService.addRefreshable(testRefreshable)
        service.startNewGame(listOf(player1, player2, player3, player4), listOf())
        rootService.currentGame.getCurrentGame().openCards = listOf(listOf(), listOf(), listOf())
        rootService.currentGame.getCurrentGame().drawCards = listOf(listOf(), listOf(), listOf())
        rootService.currentGame.getCurrentGame().tokens = MutableTokenMap()
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(1, rootService.currentGame.getCurrentGame().passingCounter)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(2, rootService.currentGame.getCurrentGame().passingCounter)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(3, rootService.currentGame.getCurrentGame().passingCounter)
        yielder = service.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(4, rootService.currentGame.getCurrentGame().passingCounter)
        assertEquals(true, testRefreshable.accessedAfterGameFinished)
    }
}