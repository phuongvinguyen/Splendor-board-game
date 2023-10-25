package view

import entity.DevelopmentCard
import entity.NobleTile
import entity.Token
import service.SourceStack
import service.TurnType
/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 *
 */

interface Refreshable {
    /**
     * performs refreshes after a game move is completed
     *
     * @param turnType NORMAL describes the completion of 'classic' game moves like reserving a card.
     * In addition, there is UNDO, the undoing of a classic game move, and REDO,
     * the redoing of a move that has been undone.
     */
    fun refreshAfterTurn(turnType: TurnType) = Unit

    /**
     * performs refreshes that are necessary after the game is ended
     *
     * @param endType: If the endType is 0, the Game is ended by Deadlocks (results a tie).
     * If endType is a 1, then the game ended 'normally', i.e. with a winner.
     */
    fun refreshAfterGameFinished(endType : Int ) = Unit

    /**
     * performs refreshes that are necessary after a token is drawn
     */
    fun refreshAfterTokenDrawn() = Unit

    /**
     * performs refreshes that are necessary after a token is discarded
     */
    fun refreshBeforeTokenDiscard() = Unit

    /**
     * performs a refresh when a noble has been assigned to the player
     *
     * @param nobleTile The nobleTile assigned to the player
     */
    fun refreshAfterNobleVisit(nobleTile: NobleTile) = Unit

    /**
     * performs a refresh when the player can choose between more than one noble tile
     *
     * @param nobleTiles The noble tiles the player can choose from
     */
    fun refreshBeforeNobleVisit(nobleTiles: List<NobleTile>) = Unit

    /**
     * performs a refresh when a DevelopmentCard has purchased
     *
     * @param card DevelopmentCard the player bought
     * @param stack The stack the DevelopmentCard is bought from
     */
    fun refreshAfterCardBought(card: DevelopmentCard, stack: SourceStack) = Unit

    /**
     * performs a refresh when a DevelopmentCard has reserved
     *
     * @param card DevelopmentCard the player reserved
     * @param source The stack the DevelopmentCard is reserved from
     */
    fun refreshAfterCardReserved(card: DevelopmentCard, source: SourceStack) = Unit

    /**
     * performs a refresh when the player has to discard tokens
     *
     * @param tokens The tokens that the player wants to discard
     */
    fun refreshAfterTokenDiscard(tokens: List<Token>) = Unit
}