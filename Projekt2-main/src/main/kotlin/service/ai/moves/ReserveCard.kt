package service.ai.moves

import entity.DevelopmentCard
import entity.GameState
import service.RootService
import service.SourceStack

/**
 * This move reserves a card from a specific stack
 */
class ReserveCard(
    private val card: DevelopmentCard,
    private val sourceStack: SourceStack,
    selectNobelMove: SelectNoble?,
    returnTokenMove: ReturnTokens?,
) : Move(selectNobelMove, returnTokenMove) {
    /**
     * Performs this move.
     * @param game the current game state this move should be performed
     * on
     * @return a [GameState] instance that represents the game state
     * **after** this move has been performed (regardless of success,
     * though a failure might result in an exception instead)
     */
    override fun perform(service: RootService) {
        service.playerActionService.reserveCard(card, sourceStack, true)
        completeTurn(service)
    }

    /**
     * the message displayed after the move was made
     */
    override fun asLogMessage() = "Reserve card $card"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ReserveCard

        if (card != other.card) return false
        if (sourceStack != other.sourceStack) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + sourceStack.hashCode()
        return result
    }

}