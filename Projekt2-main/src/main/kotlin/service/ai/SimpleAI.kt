package service.ai

import entity.GameState
import entity.Token
import service.ai.moves.*

/**
 * This AI performs only random moves
 */
open class SimpleAI : AI {
    /**
     * Returns a random (but possible) move based on the current
     * [gameState]. No magic or critical thinking involved. It might
     * pick the smartest option available (or the polar opposite).
     * @random a random possible move based on [gameState]
     */
    override fun determineBestMove(gameState: GameState, maxTimeMs: Long): Move {
        val possibleMoves = listOf(0, 1, 2).shuffled().toMutableList()
        while (possibleMoves.isNotEmpty()) {
            val move = when (possibleMoves.removeLast()) {
                0 -> buyRandomCard(gameState)
                1 -> drawRandomTokens(gameState)
                2 -> reserveRandomCard(gameState)
                else -> throw IllegalStateException("Unreachable")
            }
            if (move != null) {
                return move
            }
        }
        return Pass(getSelectNobleMove(gameState))
    }

    private fun buyRandomCard(game: GameState): Move? {
        val cardOptions = getAffordableCards(game, game.players[game.currentPlayer])

        if (cardOptions.isEmpty()) {
            return null
        }
        val card = cardOptions.random()
        val player = game.players[game.currentPlayer]

        // Just pay the minimum amount
        val payment = getMinimumPayment(player, card.first)

        val selectNobleMove = getSelectNobleMove(game, listOfNotNull(card.first.bonus))
        return BuyCard(card.first, card.second, payment, selectNobleMove)
    }

    private fun drawRandomTokens(game: GameState) = when ((0..1).random()) {
        // If it is not possible to draw two same tokens, draw three different tokens
        0 -> drawRandomTwoTokens(game) ?: drawRandomThreeTokens(game)
        1 -> drawRandomThreeTokens(game)
        else -> throw IllegalStateException("Unreachable")
    }

    private fun drawRandomTwoTokens(game: GameState): Move? {
        val possibleTokens = game.tokens.filter { (token, amt) -> amt >= 4 && token != Token.GOLD }.toList()
        if (possibleTokens.isEmpty()) {
            return null
        }

        val token = possibleTokens.random().first

        // check if tokens must be returned
        val player = game.players[game.currentPlayer]
        val diff = player.totalTokens() + 2 - 10
        val returnTokenMove = if (diff > 0) {
            // Just put some tokens back
            ReturnTokens(List(diff) { token })
        } else {
            null
        }

        val selectNobleMove = getSelectNobleMove(game)
        return DrawTwoTokens(token, selectNobleMove, returnTokenMove)
    }

    private fun drawRandomThreeTokens(game: GameState): Move? {
        val player = game.players[game.currentPlayer]

        if (player.totalTokens() >= 10) {
            return null
        }

        val possibleTokens =
            game.tokens.filter { (token, amt) -> amt >= 1 && token != Token.GOLD }.map { it.first }.toList()
        if (possibleTokens.isEmpty()) {
            return null
        }

        val tokens = possibleTokens.shuffled().take(3)

        // check if tokens must be returned
        val diff = player.totalTokens() + tokens.size - 10
        val returnTokenMove = if (diff > 0) {
            // Just put some tokens back
            ReturnTokens(List(diff) { tokens[it] })
        } else {
            null
        }

        val selectNobleMove = getSelectNobleMove(game)
        return DrawThreeTokens(tokens, selectNobleMove, returnTokenMove)
    }

    private fun reserveRandomCard(game: GameState): Move? {
        val player = game.players[game.currentPlayer]
        if (player.reservedCards.size >= 3) {
            return null
        }

        val possibleCards = getAvailableReserveCards(game)
        if (possibleCards.isEmpty()) {
            return null
        }

        val selection = possibleCards.random()

        // Put an arbitrary token back if it is too much
        val returnTokensMove = if (player.totalTokens() >= 10) {
            val returnableTokens = player.tokens.toMutableTokenMap()
            returnableTokens += Token.GOLD
            val returnedToken =
                returnableTokens
                    .filter { it.second > 0 }.flatMap { (token, amt) -> List(amt) { token } }
                    .toList()
                    .random()
            ReturnTokens(listOf(returnedToken))
        } else {
            null
        }

        val selectNobleMove = getSelectNobleMove(game)
        return ReserveCard(selection.first, selection.second, selectNobleMove, returnTokensMove)
    }

    protected fun getSelectNobleMove(game: GameState, extraBonuses: List<Token> = listOf()): SelectNoble? {
        val availableNobles = getAvailablePlayerNobles(game, game.players[game.currentPlayer], extraBonuses)
        return if (availableNobles.size > 1) {
            SelectNoble(availableNobles.random())
        } else {
            null
        }
    }
}

















