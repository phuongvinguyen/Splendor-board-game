package entity

import kotlinx.serialization.Serializable

/**
 * This class manages a list of game states, to allow walking through them.
 * Additionally, it stores whether players are still allowed to be added to the highscore list
 */
@Serializable
data class GameManager(
    var currentState: GameState,
    val gameStates: MutableList<GameState> = mutableListOf(currentState.copy()),
    var currentGameIndex: Int = 0,
    var allowHighscore: Boolean = true, val gameHistory: MutableList<String> = mutableListOf()
) {
    /**
     * @return The current active game
     */
    fun getCurrentGame() = currentState

    /**
     * This method adds a round to this game manager*
     */
    fun addRound() {
        if (currentGameIndex < gameStates.size - 1) {
            gameStates.removeIf { gameStates.indexOf(it) > currentGameIndex }
        }
        gameStates.add(currentState.copy())
        currentGameIndex = gameStates.size - 1
    }
}