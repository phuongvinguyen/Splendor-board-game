package service.ai.moves

import entity.GameState
import service.RootService

/**
 * This abstract class represents a move that can be made in this game
 */
abstract class Move(private val selectNobleMove: SelectNoble?, private val returnTokensMove: ReturnTokens? = null) {
    /**
     * Performs the move on the current game and returns the next state
     *
     * @param game The game state to performt his move on
     * @return The game state after this move was performed
     */
    abstract fun perform(rootService:RootService)

    /**
     * Performs the move on the current game and returns the next state
     *  @param gameState The game state to performt his move on
     * @return The game state after this move was performed
     */
     fun performOnGameState(gameState: GameState):GameState{
         val service=RootService(gameState.copy())
         perform(service)
         return service.currentGame.getCurrentGame()
     }

    /**
     * In order to display a move as a text, e.g. in the show tip function or the move history, this method is required
     */
    abstract fun asLogMessage(): String

    /**
     * This function completes the turn so the next player can take action.
     * This can include selecting a noble and returning some tokens
     *
     * @param service The service that is associated with the yielder
     * @throws NullPointerException If a special move is required, but not provided
     */
    fun completeTurn(service: RootService) {
        if (service.gameService.getCurrentPlayer().totalTokens() > 10) {
            returnTokensMove!!.perform(service)
        }
        service.gameService.startNextRoundInner(selectNobleMove?.noble, true)
    }
    /**
     * the message displayed after the move was made
     */
    override fun toString(): String {
        var string = asLogMessage()
        if (returnTokensMove != null) {
            string += " and " + returnTokensMove.asLogMessage()
        }
        if (selectNobleMove != null) {
            string += " and " + selectNobleMove.logMessage
        }
        return string
    }

    /**
     * a function to enable comparison
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Move

        if (selectNobleMove != other.selectNobleMove) return false
        if (returnTokensMove != other.returnTokensMove) return false

        return true
    }

    /**
     *
     */
    override fun hashCode(): Int {
        var result = selectNobleMove?.hashCode() ?: 0
        result = 31 * result + (returnTokensMove?.hashCode() ?: 0)
        return result
    }
}