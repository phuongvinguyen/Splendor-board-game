package service.ai.moves

import service.RootService

/**
 * This move just does nothing but ending the current players turn
 */
class Pass(selectNobleMove: SelectNoble?): Move(selectNobleMove) {
    /**
     * preforms the pass Action
     */
    override fun perform(service: RootService) {
        completeTurn(service)
    }
    /**
     * the message displayed after the move was made
     */
    override fun asLogMessage() = "Pass"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    // Required, otherwise intellij gets angry
    override fun hashCode(): Int {
        return super.hashCode()
    }
}