package service

import entity.*
import kotlin.test.*

/**
 * Class that provides tests for [PlayerActionService]
 */

class PlayerActionServiceTest {

    val names = listOf(Player("Eros", PlayerType.HUMAN), Player("Steven", PlayerType.EASY))
    val names2 = listOf(Player("Amir", PlayerType.HUMAN), Player("Vi", PlayerType.EASY))
    val names3 = listOf(Player("Projektleiter", PlayerType.HUMAN), Player("Valerij", PlayerType.EASY))
    val names4 = listOf(Player("David", PlayerType.HUMAN), Player("Julian", PlayerType.EASY))
    val names5 = listOf(Player("Medium AI", PlayerType.MEDIUM), Player("Simple AI", PlayerType.EASY))
    val game = RootService()

    /**
     * Test if the game starts properly and if it's possible to even play
     */
    @Test
    fun testNewGame() {
        game.gameService.startNewGame(names, listOf())
        val players = game.currentGame.getCurrentGame().players
        assertNotNull(players)
        val Eros = players.count { it.name == "Eros" }
        assertNotEquals(0, Eros)
        assertEquals(1, Eros)
        val tokens = game.currentGame.getCurrentGame().tokens
        assertNotEquals(0, tokens[Token.DIAMOND])
    }

    /**
     * A test for the Select Tokens Action
     */
    @Test
    fun testSelectTokens() {
        val tokensToTake = listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD)
        val tokensWanted = listOf(Token.ONYX, Token.ONYX, Token.EMERALD)
        game.gameService.startNewGame(names, listOf())
        val currentPlayer = game.currentGame.getCurrentGame().players[game.currentGame.getCurrentGame().currentPlayer]

        game.playerActionService.selectTokens(tokensToTake) // player gets one diamond,ruby,emerald
        val filteredTokenList = currentPlayer.tokens.filter { (token, amt) -> tokensToTake.contains(token) }.toList()
        //check that the player only has 3 tokens
        assertEquals(3, filteredTokenList.size)
        filteredTokenList.forEach { assertEquals(1, it.second) }

        game.playerActionService.selectTokens(tokensToTake)
        assertFails { game.playerActionService.selectTokens(tokensWanted) } // not all tokens are different

        val newTokensWanted = listOf(Token.ONYX, Token.ONYX)
        game.playerActionService.selectTokens(newTokensWanted)

        assertFails { game.playerActionService.selectTokens(newTokensWanted) } // less than 4 tokens in stack
        val playerFiltered = game.currentGame.getCurrentGame().players.filter { it.tokens.get(Token.ONYX) == 2 }
        assertEquals(1, playerFiltered.size) // only one stack of tokens should have two tokens

        game.playerActionService.selectTokens(tokensToTake)
        game.playerActionService.selectTokens(tokensToTake)

        // Special cases
        val tmp = listOf(Token.ONYX, Token.SAPPHIRE)
        val doubleSapphire = listOf(Token.SAPPHIRE, Token.SAPPHIRE)
        game.playerActionService.selectTokens(tmp)
        assertFails { game.playerActionService.selectTokens(doubleSapphire) }
        game.playerActionService.selectTokens(tmp)
        val newTmp = tmp.drop(1)
        game.playerActionService.selectTokens(newTmp)
        game.playerActionService.selectTokens(newTmp)
        assertEquals(1, game.currentGame.getCurrentGame().tokens.filter { it.second > 0 }.toList().size)
    }

    /**
     * A Test for the Buy Card Action
     */

    @Test
    fun buyCardTest() {
        game.gameService.startNewGame(names, listOf())
        var yielder: Yielder<Unit>?
        val player1 = game.currentGame.getCurrentGame().players[0]
        val card1ToBuy = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 4, Token.SAPPHIRE to 4, Token.EMERALD to 4).toTokenMap(),
            bonus = Token.DIAMOND,
            prestigePoints = 0,
            tier = 1,
            imageID = 1
        )
        val card2 = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.SAPPHIRE,
            prestigePoints = 0,
            tier = 1,
            imageID = 1
        )
        val card3ToBuy = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.EMERALD,
            prestigePoints = 0,
            tier = 1,
            imageID = 1
        )

        val highCardToBuy = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.EMERALD,
            prestigePoints = 3,
            tier = 1,
            imageID = 1
        )
        val newTierOneStack = listOf(card1ToBuy, card2, highCardToBuy, card3ToBuy)
        val tmpList = game.currentGame.getCurrentGame().openCards.toMutableList()
        tmpList[0] = newTierOneStack
        game.currentGame.getCurrentGame().openCards = tmpList

        // we create a card we can track
        game.currentGame.getCurrentGame().players[0].tokens = card1ToBuy.cost
        game.playerActionService.buyCard(
            card2, game.currentGame.getCurrentGame().players[0].tokens, SourceStack.OPEN_TIER1
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(player1.cards.size, 1) //player should have one card(the one bought)
        assertEquals(player1.cards[0], card2)

        game.currentGame.getCurrentGame().openCards = tmpList
        val tmpCard = game.currentGame.getCurrentGame().openCards[0][1]
        game.gameService.getCurrentPlayer().tokens =
            card1ToBuy.cost // make sure the player have the correct amount of tokens

        game.playerActionService.buyCard(
            game.currentGame.getCurrentGame().openCards[0][1],
            game.currentGame.getCurrentGame().players[1].tokens,
            SourceStack.OPEN_TIER1
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertContains(game.currentGame.getCurrentGame().players[1].cards, tmpCard)
        assertEquals(4, game.currentGame.getCurrentGame().openCards[0].size)

        val card4 = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.ONYX,
            prestigePoints = 0,
            tier = 1,
            imageID = 1
        )
        val card5 = DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.RUBY,
            prestigePoints = 0,
            tier = 1,
            imageID = 1
        )
        game.currentGame.getCurrentGame().players[0].cards =
            listOf(card1ToBuy, card2, card3ToBuy, card4, card5) // give the player 3 cards
        game.currentGame.getCurrentGame().players[0].tokens =
            card1ToBuy.cost // make sure the player has the tokens to buy the card
        game.currentGame.getCurrentGame().openCards = tmpList
        val highCard = game.currentGame.getCurrentGame().openCards[0][1]
        assertEquals(game.currentGame.getCurrentGame().players[0], game.gameService.getCurrentPlayer())
        assertEquals(game.currentGame.getCurrentGame().openCards[0][1], tmpList[0][1])
        game.playerActionService.buyCard(
            game.currentGame.getCurrentGame().openCards[0][1], MutableTokenMap(), SourceStack.OPEN_TIER1
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(
            game.currentGame.getCurrentGame().players[0].cards.size, 6
        ) // player should have 4 cards after the purchase
        assertContains(
            game.currentGame.getCurrentGame().players[0].cards, highCard
        ) // make sure the player actually have the card


    }

    /**
     * A test for the Reserve Card Action
     */

    @Test
    fun reserveCardTest() {
        game.gameService.startNewGame(names, listOf())
        var yielder: Yielder<Unit>?
        val player1 = game.currentGame.getCurrentGame().players[0]


        assertEquals(5, game.currentGame.getCurrentGame().tokens[Token.GOLD])

        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(1, game.currentGame.getCurrentGame().currentPlayer)

        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][2], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(
            game.currentGame.getCurrentGame().players[1].reservedCards.size,
            game.currentGame.getCurrentGame().players[0].reservedCards.size
        )
        //assertEquals(3, game.currentGame.tokens[Token.GOLD])
        assertEquals(1, player1.tokens[Token.GOLD])

        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][0], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(
            game.currentGame.getCurrentGame().openCards[0].size, 4
        )
        // player 1 should have more gold than player2 because there are only 5 gold tokens
        assertNotEquals(
            game.currentGame.getCurrentGame().players[0].tokens[Token.GOLD],
            game.currentGame.getCurrentGame().players[1].tokens[Token.GOLD]
        )
        assertFails {
            game.playerActionService.reserveCard(
                game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
            )
        }


    }

    /**
     * Tests the undo and redo function
     */
    @Test
    fun undoAndRedoTest() {
        game.gameService.startNewGame(names, listOf())
        var yielder: Yielder<Unit>?

        assertEquals(5, game.currentGame.getCurrentGame().tokens[Token.GOLD])

        val copyCard = game.currentGame.getCurrentGame().openCards[2][1]

        game.playerActionService.reserveCard(
            game.currentGame.getCurrentGame().openCards[2][1], SourceStack.OPEN_TIER3
        )
        yielder = game.gameService.startNextRound()
        while (yielder.hasNext()) {
            yielder.continueWith(null)
        }
        assertEquals(1, game.currentGame.getCurrentGame().currentPlayer)
        assertContains(game.currentGame.getCurrentGame().players[0].reservedCards, copyCard )

        game.playerActionService.undo()

        assertEquals(0, game.currentGame.getCurrentGame().currentPlayer)
        assertEquals(game.currentGame.getCurrentGame().players[0].reservedCards.size, 0 )

        game.playerActionService.redo()

        assertEquals(1, game.currentGame.getCurrentGame().currentPlayer)
        assertContains(game.currentGame.getCurrentGame().players[0].reservedCards, copyCard )

    }

    /**
     * Test if the game properly loads and saves
     */

    @Test
    fun saveAndLoadGame() {
        repeat(1) {
            game.gameService.startNewGame(names, listOf())
            var yielder: Yielder<Unit>?

            game.playerActionService.selectTokens(listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD))
            yielder = game.gameService.startNextRound()
            while (yielder.hasNext()) {
                yielder.continueWith(null)
            }

            val gameBeforeSaving = game.currentGame
            game.playerActionService.saveGame(it + 1)

            val secondGameNames = when (it) {
                0 -> names2
                1 -> names3
                2 -> names4
                3 -> names5
                else -> names2
            }
            game.gameService.startNewGame(secondGameNames, listOf())
            game.playerActionService.selectTokens(listOf(Token.SAPPHIRE, Token.ONYX, Token.DIAMOND))
            yielder = game.gameService.startNextRound()

            while (yielder.hasNext()) {
                yielder.continueWith(null)
            }

            game.playerActionService.loadGame(it + 1)

            assertEquals(gameBeforeSaving.getCurrentGame().players, game.currentGame.getCurrentGame().players)
        }

    }

    /**
     * test the get Hint Move
     */
    @Test
    fun getHintTest() {
        val test = "this is a test"
        var hint = "this is a test"
        game.gameService.startNewGame(names, listOf())
        game.playerActionService.selectTokens(listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD))
        game.gameService.startNextRound()
        game.playerActionService.selectTokens(listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD))
        game.gameService.startNextRound()
        assertEquals(0, game.currentGame.getCurrentGame().currentPlayer)
        hint = game.playerActionService.getHint()
        assertNotEquals(test, hint)
        assertEquals(0, game.currentGame.getCurrentGame().currentPlayer)
        print(hint)
    }

    /**
     * test for the specific situation when a player mus choose between nobles
     */
    @Test
    fun selectNobleTest() {
        game.gameService.startNewGame(names, listOf())
        val noble = game.currentGame.getCurrentGame().nobleTiles[1]
        val wrongNoble = NobleTile(
            requirements = mapOf(
                Token.DIAMOND to 4, Token.SAPPHIRE to 4, Token.EMERALD to 4, Token.GOLD to 4
            ).toTokenMap(), imageID = noble.imageID, prestigePoints = 4
        )
        game.playerActionService.selectTokens(listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD))
        assertEquals(0, game.currentGame.getCurrentGame().currentPlayer)
        game.playerActionService.selectNoble(noble)
        assertEquals(0, game.currentGame.getCurrentGame().currentPlayer)
        assertContains(game.gameService.getCurrentPlayer().nobleTiles, noble)
        assertEquals(2, game.currentGame.getCurrentGame().nobleTiles.size)
        game.playerActionService.selectTokens(listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD))
        assertFails { game.playerActionService.selectNoble(wrongNoble) }
        assertEquals(2, game.currentGame.getCurrentGame().nobleTiles.size)
    }

    /**
     * test if the return tokens action works properly
     */
    @Test
    fun returnTokensTest() {
        val tokensToTake = listOf(Token.DIAMOND, Token.RUBY, Token.EMERALD)
        game.gameService.startNewGame(names, listOf())
        game.playerActionService.selectTokens(tokensToTake)
        tokensToTake.forEach {
            assertNotEquals(0, game.currentGame.getCurrentGame().players[0].tokens[it])
        }
        game.currentGame.getCurrentGame().currentPlayer = 0
        assertEquals(game.currentGame.getCurrentGame().players[0], game.gameService.getCurrentPlayer())
        game.playerActionService.returnTokens(tokensToTake)
        assertEquals(0, game.currentGame.getCurrentGame().players[0].tokens.filter { it.second > 0 }.toList().size)
        assertEquals(0, game.currentGame.getCurrentGame().tokens.filter { it.second < 4 }.toList().size)
    }

}