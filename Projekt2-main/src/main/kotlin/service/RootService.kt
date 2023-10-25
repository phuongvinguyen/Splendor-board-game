package service

import entity.GameManager
import entity.GameState
import view.Refreshable

/**
 * Main class of the service layer for the War card game. Provides access
 * to all other service classes and holds the [currentGame] state for these
 * services to access.
 */

class RootService(var currentGame: GameManager = GameManager(GameState())) {
    constructor(gameState: GameState) : this(GameManager(gameState))

    val playerActionService: PlayerActionService = PlayerActionService(this)
    val gameService: GameService = GameService(this)
    val checkPlayerMove: CheckPlayerMove = CheckPlayerMove(this)

    /**
     * adds a new refreshable
     * @param newRefreshable is the refreshable to be added
     */
    fun addRefreshable(newRefreshable: Refreshable){
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
    }
}