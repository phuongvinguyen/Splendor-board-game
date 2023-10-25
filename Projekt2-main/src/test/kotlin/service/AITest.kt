package service

import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.Test
import service.ai.*
import service.ai.moves.Pass
import kotlin.system.measureTimeMillis


/**
 * a class to test if the AI works properly
 */
class AITest {
    /**
     * a class to keep track of the results of each game simulated
     */
    class GameCompletion(val result: GameResult, val turns: Int)

    /**
     * an AI Player, with an [implementation] hard, medium or easy and [turnTimeMs]
     * with the amount of time each turn should take
     */
    class AIPlayer(val implementation: AI, val turnTimeMs: Long, val name: String)

    /**
     * This function runs an AI game to completion and returns the result
     * The [GameResult] is always talking about the first player
     */
    fun runGameToCompletion(players: List<AIPlayer>, showLog: Boolean): GameCompletion {
        val service = RootService()
        service.gameService.startNewGame(
            players.map { Player(name = it.name, playerType = PlayerType.MEDIUM) }, listOf()
        )

        var numberOfRounds = 0
        var passCounter = 0
        while (!service.gameService.checkGameEnd()) {
            numberOfRounds += 1
            val ai = players[service.currentGame.getCurrentGame().currentPlayer]
            val move = ai.implementation.determineBestMove(service.currentGame.getCurrentGame(), ai.turnTimeMs)

            if (move is Pass) {
                passCounter += 1
            } else {
                passCounter = 0
            }
            if (passCounter == service.currentGame.getCurrentGame().players.size) {
                return GameCompletion(
                    GameResult.DRAW,
                    numberOfRounds / service.currentGame.getCurrentGame().players.size
                )
            }


            move.perform(service)
           // assertNotEquals(manager, service.currentGame.getCurrentGame())
        }

        val winningPlayerIdx =
            service.currentGame.getCurrentGame().players.indices.maxByOrNull {
                service.gameService.calculatePoints(service.currentGame.getCurrentGame().players[it])
            }!!
        if (showLog) {
            println("${players[winningPlayerIdx].name} Wins!")
        }
        val result = when (winningPlayerIdx) {
            0 -> GameResult.WIN
            else -> GameResult.LOSS
        }
        return GameCompletion(result, numberOfRounds / service.currentGame.getCurrentGame().players.size)
    }

    /**
     * create Two AI players and set them up to  play against each other
     * at the end of each turn the move made by the AI is printed
     * at the end of the game the current number of wins and losses and the average turn per game
     * are printed
     */

    @Test
    fun aiGameTest() {
        val players = listOf(
            AIPlayer(MediumAI(), 500, "MediumAI"),
            AIPlayer(SimpleAI(), 500, "SimpleAI"),
            AIPlayer(HardAI(), 500, "HardAI")
        )
        val results = GameResult.values().associateWith { 0 }.toMutableMap()

        val gameLengths = mutableListOf<Double>()
        val elapsed = measureTimeMillis {
            repeat(1) {
                val completion = runGameToCompletion(players, true)
                results[completion.result] = results[completion.result]!! + 1
                gameLengths += completion.turns.toDouble()

                println("------------------- WINNER: ${completion.result}")
                println(results)
                println(
                    "Average game length: ${gameLengths.average()} turns [min=${gameLengths.minOrNull()}," +
                            "max=${gameLengths.maxOrNull()}]"
                )
                println()
            }
        }

        println("Elapsed time: $elapsed ms")

    }
}