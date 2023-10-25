package service.ai

import entity.Player
import entity.PlayerType
import service.RootService
import kotlin.system.measureTimeMillis

/**
 * Saves the [amount of turns][turns] it took to achieve this
 * game's [result].
 * @param result the [result of the game][GameResult]
 * @param turns the amount of turns that were required to achieve
 * the game's [result]
 */
class GameCompletion(val result: GameResult, val turns: Int, val log: List<String>)

/**
 * Represents an AI that plays the game instead of a human.
 * It saves its maximum allowed turn time and more importantly its name.
 * @param implementation the [AI] instance that controls this player
 * @param turnTimeMs the maximum allowed time for taking a turn
 * @param name the name of the player
 */
class AIPlayer(val implementation: AI, val turnTimeMs: Long, val name: String)

/**
 * This function runs an AI game to completion and returns the result
 * The [GameResult] is always indicating the success of the first player
 * in the list of scores, meaning a draw will still result in a win,
 * if the secondary (or tertiary) win conditions are met.
 * @param players the [List] of [AIPlayer]s
 * @param showLog whether to print the actions done by the AIs in every
 * turn
 */
fun runGameToCompletion(players: List<AIPlayer>, showLog: Boolean): GameCompletion {
    val log = mutableListOf<String>()
    val service = RootService()
    service.gameService.startNewGame(
        players.map { Player(name = it.name, playerType = PlayerType.MEDIUM) }, listOf()
    )

    var numberOfRounds = 0
    while (!service.gameService.checkGameEnd()) {
        numberOfRounds += 1
        val ai = players[service.currentGame.getCurrentGame().currentPlayer]
        val move = ai.implementation.determineBestMove(service.currentGame.getCurrentGame(), ai.turnTimeMs)

        val playerName = ai.name
        val playerTokens = service.gameService.getCurrentPlayer().tokens

        if (showLog) {
            log.add(
                "$playerName has $playerTokens, prestige: " + "" +
                        "${service.gameService.calculatePoints(service.gameService.getCurrentPlayer())}"
            )
            log.add("$move\n")
        }

        move.perform(service)

        val game = service.currentGame.getCurrentGame()
        if (game.passingCounter == game.players.size) {
            return GameCompletion(
                GameResult.DRAW, numberOfRounds / service.currentGame.getCurrentGame().players.size, log
            )
        }
    }

    val winningPlayerIdx = service.gameService.getBestPlayerIndex()
    if (showLog) {
        log.add("${players[winningPlayerIdx].name} Wins!")
    }
    val result = when (winningPlayerIdx) {
        0 -> GameResult.WIN
        else -> GameResult.LOSS
    }
    return GameCompletion(result, numberOfRounds / service.currentGame.getCurrentGame().players.size, log)
}

/**
 * Runs a specific amount of games of AI vs. AI to find bugs
 * or analyse the results. It calls [runGameToCompletion] with
 * parameter `showLogs` set to `true` to provide
 * additional information and then prints an aggregated statistic
 * of all games detailing the exact amount of wins, losses, draws
 * and the resulting win rate for the AI (player) in the
 * first player slot. The maximum and minimum length of any game
 * played is also printed, along with the average of the two.
 */
fun main() {
    val players = listOf(
        AIPlayer(RandomWeightedAI(), 1000, "WeightedAI"),
        AIPlayer(SimpleAI(), 1000, "SimpleAI"),
    )
    val results = GameResult.values().associateWith { 0 }.toMutableMap()

    val gameLengths = mutableListOf<Double>()
    val elapsed = measureTimeMillis {
        repeat(10000) {
            val completion = runGameToCompletion(players, true)
            results[completion.result] = results[completion.result]!! + 1
            if (completion.result != GameResult.DRAW)
                gameLengths += completion.turns.toDouble()

            println(completion.log.joinToString("\n"))
            println("------------------- WINNER: ${completion.result}")
            println(results)
            println(
                "Average game length: ${gameLengths.average()} turns [min=${gameLengths.minOrNull()}" +
                        ",max=${gameLengths.maxOrNull()}]"
            )
            println()
        }
    }

    println("Elapsed time: $elapsed ms")
}