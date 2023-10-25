package service.ai

import entity.GameState
import entity.Token
import service.ai.moves.*
import kotlin.math.max
import kotlin.random.Random

const val DEFAULT_WEIGHT: Double = 10.0

/**
 * This AI is similar to [SimpleAI], but a bit smarter, because it weights its moves according
 * to some predefined rules.
 */
class RandomWeightedAI : SimpleAI() {
    private val random: Random = Random(System.currentTimeMillis())

    override fun determineBestMove(gameState: GameState, maxTimeMs: Long): Move {
        val weightedMoves: MutableList<Pair<Move, Double>> = mutableListOf()
        weightedMoves += getReserveCardMoves(gameState)
        weightedMoves += getBuyCardMoves(gameState)
        weightedMoves += getDrawThreeTokensMove(gameState)
        weightedMoves += getDrawTwoTokensMove(gameState)

        val cumWeight = weightedMoves.sumOf { it.second }
        var weightThreshold = random.nextDouble() * cumWeight
        var selectedMove: Move = Pass(null)

        for (move in weightedMoves) {
            weightThreshold -= move.second
            if (weightThreshold <= 0) {
                selectedMove = move.first
                break
            }
        }

        return selectedMove
    }

    private fun getReserveCardMoves(state: GameState): List<Pair<Move, Double>> {
        val player = state.players[state.currentPlayer]

        if (player.reservedCards.size >= 3) {
            return listOf()
        }

        val possibleCards = getAvailableReserveCards(state)
        if (possibleCards.isEmpty()) {
            return listOf()
        }

        val returnTokensMove = if (player.totalTokens() >= 10) {
            val returnableTokens = player.tokens.toMutableTokenMap()
            // Don't put the gold back, because gold is the best token kind
            val returnedToken =
                returnableTokens
                    .filter { it.second > 0 }.flatMap { (token, amt) -> List(amt) { token } }
                    .toList()
                    .random()
            ReturnTokens(listOf(returnedToken))
        } else {
            null
        }

        val selectNobleMove = getSelectNobleMove(state)

        return possibleCards.map {
            val move = ReserveCard(it.first, it.second, selectNobleMove, returnTokensMove)
            if (returnTokensMove != null) {
                Pair(move, DEFAULT_WEIGHT * 0.80)
            } else {
                Pair(move, DEFAULT_WEIGHT)
            }
        }
    }

    private fun getBuyCardMoves(state: GameState): List<Pair<Move, Double>> {
        val player = state.players[state.currentPlayer]
        val affordableCards = getAffordableCards(state, player)

        // TODONE: ... weighting?! Masterfully solved.
        return affordableCards.map {
            val selectNobleMove = getSelectNobleMove(state, listOfNotNull(it.first.bonus))
            val move = BuyCard(it.first, it.second, getMinimumPayment(player, it.first), selectNobleMove)
            val weight = DEFAULT_WEIGHT * (1.0 + it.first.prestigePoints)
            /*when(player.getPrestigePoints()) {
                in 0..2 ->
                else -> 8 + 1.0 * it.first.prestigePoints
            }*/
            Pair(move, weight)
        }
    }

    private fun getDrawThreeTokensMove(state: GameState): List<Pair<Move, Double>> {
        val player = state.players[state.currentPlayer]

        var drawTokenBaseWeight = DEFAULT_WEIGHT

        val totalPlayerTokens = player.totalTokens()
        if (totalPlayerTokens >= 10) {
            drawTokenBaseWeight *= 0.2
        } else if (totalPlayerTokens == 9) {
            drawTokenBaseWeight *= 0.5
        } else if (totalPlayerTokens == 8) {
            drawTokenBaseWeight *= 0.8
        }

        val availableMidTokens = state.tokens.count { (token, amt) ->
            amt > 0 && token != Token.GOLD
        }

        val result: MutableList<Pair<Move, Double>> = mutableListOf()

        val selectNobleMove = getSelectNobleMove(state)
        for (combination in AI.TOKEN_COMBINATIONS) {
            val availableTokensOfCombination = combination.filter { state.tokens[it] > 0 }
            if (availableTokensOfCombination.size < minOf(3, availableMidTokens)
                || availableTokensOfCombination.isEmpty()) {
                continue
            }
            // in case of having too many tokens, random tokens are chosen to get
            // rid of
            val excessTokens = (player.totalTokens() + availableTokensOfCombination.size) - 10
            val returnTokensMove = if (excessTokens > 0) {
                val playerTokens = player.tokens.flatMap { (token, amt) -> List(amt) { token } }
                val selectedTokens = playerTokens.shuffled().take(excessTokens).toList()
                ReturnTokens(selectedTokens)
            } else {
                null
            }

            // Don't draw tokens if there are already many of them
            val badTokenFactor = max(0.2, 1 - availableTokensOfCombination.count { player.tokens[it] >= 4 } * 0.3)

            result += Pair(
                DrawThreeTokens(availableTokensOfCombination.toList(), selectNobleMove, returnTokensMove),
                drawTokenBaseWeight * badTokenFactor
            )
        }
        return result
    }

    private fun getDrawTwoTokensMove(state: GameState): List<Pair<Move, Double>> {
        val player = state.players[state.currentPlayer]

        if (player.totalTokens() >= 9) {
            return listOf()
        }
        val result: MutableList<Pair<Move, Double>> = mutableListOf()

        val selectNobleMove = getSelectNobleMove(state)
        for ((token, amt) in state.tokens) {
            if (amt < 4 || token == Token.GOLD) {
                continue
            }
            val drawTokenWeight = if (player.tokens[token] >= 3) {
                DEFAULT_WEIGHT * 0.7
            } else {
                DEFAULT_WEIGHT
            }

            val move = DrawTwoTokens(token, selectNobleMove, null)
            result += Pair(move, drawTokenWeight)
        }

        return result
    }

}