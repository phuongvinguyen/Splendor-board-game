package entity

import kotlinx.serialization.Serializable

/**
 * Data class that represents a state game state of "Splendor".
 *
 * Since it is a DataClass, the copy() method is provided by Kotlin. Used to refer to different game states
 * in redo and undo.
 *
 * @param players List of players in this game
 * @param openCards List of lists of development cards showed open at the game table
 * @param drawCards List of lists of development cards in the deck to draw from.
 * @param nobleTiles List of noble tiles in the game
 * @param tokens Map of tokens and their quantity
 * @param currentPlayer Player currently on the turn
 * @param isLastRound Boolean. True when a player gets 15 prestige points, false otherwise
 */
@Serializable
data class GameState(
    var players: List<Player> = listOf(),
    var openCards: List<List<DevelopmentCard>> = listOf(),
    var drawCards: List<List<DevelopmentCard>> = listOf(),
    var nobleTiles: List<NobleTile> = listOf(),
    var tokens: TokenMap = MutableTokenMap(),
    var currentPlayer: Int = 0,
    var isLastRound: Boolean = false,
    var passingCounter: Int = 0,
) {
    /**
     * Copy the current game state
     */
    fun copy() = GameState(
        players = players.map { it.copy() },
        openCards = openCards,
        drawCards = drawCards,
        nobleTiles = nobleTiles,
        tokens = tokens,
        currentPlayer = currentPlayer,
        isLastRound = isLastRound,
        passingCounter = passingCounter,
    )
}