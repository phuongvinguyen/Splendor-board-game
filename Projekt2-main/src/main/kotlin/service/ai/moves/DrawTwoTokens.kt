package service.ai.moves

import entity.GameState
import entity.Token
import service.PlayerActionService
import service.RootService

/**
 * This move describes drawing two tokens of the same kind
 */
class DrawTwoTokens(
    val token: Token,
    selectNobleMove: SelectNoble?,
    returnTokensMove: ReturnTokens?
) :
    Move(selectNobleMove, returnTokensMove) {
    /**
     * Performs this move. Two tokens are attempted to be
     * taken from the game's token stacks. No checks are performed on the
     * tokens since it is expected to be correct; this would fail inside
     * [PlayerActionService.selectTokens].
     * @param game the current game state this move should be performed
     * on
     * @return a [GameState] instance that represents the game state
     * **after** this move has been performed (regardless of success,
     * though a failure might result in an exception instead)
     */
    override fun perform(service: RootService) {
        service.playerActionService.selectTokens(listOf(token, token), true)
        completeTurn(service)
    }

    /**
     * the message displayed after the move was made
     */
    override fun asLogMessage() = "Draw tokens: 2x$token"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DrawTwoTokens

        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + token.hashCode()
        return result
    }

}