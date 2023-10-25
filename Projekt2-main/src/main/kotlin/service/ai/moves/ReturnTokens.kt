package service.ai.moves

import entity.GameState
import entity.Token
import service.RootService

/**
 * This move returns tokens when there's already more than 10 tokens in hand
 */
data class ReturnTokens(val tokens: List<Token>) {
    /**
     * Performs this move.
     * @param service the service to perform this move on
     */
    fun perform(service: RootService) {
        service.playerActionService.returnTokens(tokens, false)
    }
    /**
     * the message displayed after the move was made
     */
    fun asLogMessage(): String = "return tokens: $tokens"
}