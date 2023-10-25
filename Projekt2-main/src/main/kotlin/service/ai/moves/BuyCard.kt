package service.ai.moves

import entity.DevelopmentCard
import entity.GameState
import entity.TokenMap
import service.PlayerActionService
import service.RootService
import service.SourceStack

/**
 * Representation of buying a development card used by the AI.
 * Its [perform] method internally calls
 * [PlayerActionService.buyCard].
 */
class BuyCard(
    private val card: DevelopmentCard,
    private val stack: SourceStack,
    private val payment: TokenMap,
    selectNobleMove: SelectNoble?
) : Move(selectNobleMove) {
    /**
     * Performs this move. The selected [card] is taken from
     * the source stack [stack]. The [payment] is the amount of tokens
     * taken from the current player. No checks are performed on the payment
     * since it is expected to be correct; this would fail inside
     * [PlayerActionService.buyCard].
     * @param game the current game state this move should be performed
     * on
     * @return a [GameState] instance that represents the game state
     * **after** this move has been performed (regardless of success,
     * though a failure might result in an exception instead)
     */
    override fun perform(rootService: RootService){
        rootService.playerActionService.buyCard(card, payment, stack, true)
        completeTurn(rootService)
    }

    /**
     * the message displayed after the move was made
     */
    override fun asLogMessage() =
        "Buy development card $card with $payment"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BuyCard

        if (card != other.card) return false
        if (stack != other.stack) return false
        if (payment != other.payment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + stack.hashCode()
        result = 31 * result + payment.hashCode()
        return result
    }

}