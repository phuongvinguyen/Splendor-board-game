package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [CheckPlayerMove]
 */
class CheckPlayerMoveTest {
    /**
     * The buyCardPossible function gets tested with four players
     *
     */
    @Test
    fun testBuyCardPossible() {
        val rootService = RootService()
        val checkPlayerMove = CheckPlayerMove(rootService)
        val player1 =
            Player("Test1", PlayerType.HUMAN, tokens =
            mapOf(Token.ONYX to 14, Token.SAPPHIRE to 9, Token.GOLD to 2).toTokenMap())
        val player2 = Player(
            "Test2", PlayerType.HUMAN, cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.SAPPHIRE, 5, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.ONYX, 10, 3, 0)
            ), tokens = mapOf(Token.SAPPHIRE to 10, Token.ONYX to 15).toTokenMap()
        )
        val player3 = Player(
            "Test3", PlayerType.HUMAN, cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 5, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.ONYX, 10, 3, 0)
            ), tokens = mapOf(Token.DIAMOND to 9, Token.EMERALD to 15, Token.GOLD to 2).toTokenMap()
        )
        val player4 = Player(
            "Test4", PlayerType.HUMAN, cards = listOf(
                DevelopmentCard(MutableTokenMap(), Token.DIAMOND, 5, 3, 0),
                DevelopmentCard(MutableTokenMap(), Token.ONYX, 10, 3, 0)
            ), tokens = mapOf(Token.ONYX to 14, Token.RUBY to 15, Token.GOLD to 2).toTokenMap()
        ).apply { reservedCards = listOf(DevelopmentCard(mapOf(Token.RUBY to 8, Token.ONYX to 16).toTokenMap(),
            null, 0, 0, 0)) }

        rootService.gameService.startNewGame(listOf(player1, player2, player3, player4), listOf())
        val currentGame = rootService.currentGame.getCurrentGame()
        val firstCard = DevelopmentCard(mapOf(Token.SAPPHIRE to 11, Token.ONYX to 16).toTokenMap(), null, 0, 0, 0)
        val secondCard = DevelopmentCard(mapOf(Token.DIAMOND to 11, Token.EMERALD to 16).toTokenMap(), null, 0, 0, 0)
        currentGame.openCards = listOf(listOf(firstCard, secondCard))
        //Should be false because player1 can not buy any card
        assertEquals(false, checkPlayerMove.buyCardPossible())
        currentGame.currentPlayer++
        //Should be true because player2 can buy firstCard in the openCards
        assertEquals(true, checkPlayerMove.buyCardPossible())
        currentGame.currentPlayer++
        //Should be true because player3 can buy secondCard in the openCards
        assertEquals(true, checkPlayerMove.buyCardPossible())
        currentGame.currentPlayer++
        //Should be true because player4 can buy the card in the player4's reservedCards
        assertEquals(true, checkPlayerMove.buyCardPossible())
    }

    /**
     * The selectTokensPossible function gets tested with four players
     */
    @Test
    fun testSelectTokensPossible() {
        val rootService = RootService()
        val checkPlayerMove = CheckPlayerMove(rootService)
        val player1 = Player("Test1", PlayerType.HUMAN)
        val player2 = Player("Test2", PlayerType.HUMAN)
        val player3 = Player("Test3", PlayerType.HUMAN)
        val player4 = Player("Test4", PlayerType.HUMAN)

        rootService.gameService.startNewGame(listOf(player1, player2, player3, player4), listOf())
        // Should be true because there are tokens in the middle that can be selected
        assertEquals(true, checkPlayerMove.selectTokensPossible())
        rootService.currentGame.getCurrentGame().tokens = mapOf(
            Token.EMERALD to 0,
            Token.RUBY to 0,
            Token.SAPPHIRE to 0,
            Token.DIAMOND to 0,
            Token.ONYX to 0,
            Token.GOLD to 5
        ).toTokenMap()
        // Should be false since there are no tokens in the middle
        assertEquals(false, checkPlayerMove.selectTokensPossible())
        rootService.currentGame.getCurrentGame().tokens = MutableTokenMap()
        // Should be false since there are no tokens in the middle
        assertEquals(false, checkPlayerMove.selectTokensPossible())
    }

    /**
     * The reserveCardPossible function gets tested with three players
     */
    @Test
    fun testReserveCardPossible() {
        val rootService = RootService()
        val checkPlayerMove = CheckPlayerMove(rootService)
        val firstCard = DevelopmentCard(mapOf(Token.SAPPHIRE to 11, Token.ONYX to 16).toTokenMap(), null, 0, 0, 0)
        val secondCard = DevelopmentCard(mapOf(Token.DIAMOND to 11, Token.EMERALD to 16).toTokenMap(), null, 0, 0, 0)
        val thirdCard = DevelopmentCard(mapOf(Token.RUBY to 11, Token.ONYX to 16).toTokenMap(), null, 0, 0, 0)
        val player1 = Player("Test1", PlayerType.HUMAN)
        val player2 = Player("Test2", PlayerType.HUMAN, reservedCards = listOf(firstCard, secondCard, thirdCard))
        val player3 = Player("Test3", PlayerType.HUMAN)
        rootService.gameService.startNewGame(listOf(player1, player2, player3), listOf())
        // Should be true since player1 has less than three reservedCards and there are cards in the middle
        assertEquals(true, checkPlayerMove.reserveCardPossible())
        rootService.currentGame.getCurrentGame().currentPlayer++
        // Should be false since player2 already has three reservedCards
        assertEquals(false, checkPlayerMove.reserveCardPossible())
        rootService.currentGame.getCurrentGame().drawCards = listOf(listOf())
        rootService.currentGame.getCurrentGame().openCards = listOf(listOf())
        rootService.currentGame.getCurrentGame().currentPlayer++
        // Should be false since there are no cards in the middle
        assertEquals(false, checkPlayerMove.reserveCardPossible())
        rootService.currentGame.getCurrentGame().drawCards = listOf()
        rootService.currentGame.getCurrentGame().openCards = listOf()
        rootService.currentGame.getCurrentGame().currentPlayer = 0
        // Should be false since there are no cards in the middle
        assertEquals(false, checkPlayerMove.reserveCardPossible())

    }

}