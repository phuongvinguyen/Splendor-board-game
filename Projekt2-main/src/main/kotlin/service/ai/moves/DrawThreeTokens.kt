package service.ai.moves

import entity.GameState
import entity.Token
import service.PlayerActionService
import service.RootService

/**
 * Representation of drawing (up to) 3 tokens used by the AI.
 * Its [perform] method internally calls
 * [PlayerActionService.selectTokens].
 */
class DrawThreeTokens(
    val tokens: List<Token>,
    selectNobleMove: SelectNoble?,
    returnTokensMove: ReturnTokens?) :
    Move(selectNobleMove, returnTokensMove) {
    /**
     * Performs this move. The amount of [tokens] are attempted to be
     * taken from the game's token stacks. No checks are performed on the
     * [tokens] since it is expected to be correct; this would fail inside
     * [PlayerActionService.selectTokens].
     * @param game the current game state this move should be performed
     * on
     * @return a [GameState] instance that represents the game state
     * **after** this move has been performed (regardless of success,
     * though a failure might result in an exception instead)
     */
    override fun perform(rootService: RootService){
        rootService.playerActionService.selectTokens(tokens, true)
        completeTurn(rootService)
    }

    /**
     * the message displayed after the move was made
     */
    override fun asLogMessage() = "Draw tokens ${tokens.joinToString()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DrawThreeTokens

        if (tokens != other.tokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + tokens.hashCode()
        return result
    }

}