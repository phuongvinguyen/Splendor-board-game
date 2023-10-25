package service

import entity.DevelopmentCard
import entity.NobleTile
import entity.Token
import view.Refreshable

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called
 */
class TestRefreshable : Refreshable {
    var accessedAfterTurn = false
    var accessedAfterGameFinished = false
    var accessedAfterTokenDrawn = false
    var accessedBeforeTokenDiscard = false
    var accessedAfterNobleVisit = false
    var accessedBeforeNobleVisit = false
    var accessedAfterCardBought = false
    var accessedAfterCardReserved = false
    var accessedAfterTokenDiscard = false


    /**
     * Resets all variables that remember if a refresh method has been called
     */
    fun reset() {
        accessedAfterTurn = false
        accessedAfterGameFinished = false
        accessedAfterTokenDrawn = false
        accessedBeforeTokenDiscard = false
        accessedAfterNobleVisit = false
        accessedBeforeNobleVisit = false
        accessedAfterCardBought = false
        accessedAfterCardReserved = false
        accessedAfterTokenDiscard = false
    }

    override fun refreshAfterTurn(turnType: TurnType) {
        accessedAfterTurn = true
    }

    override fun refreshAfterGameFinished(endType : Int) {
        accessedAfterGameFinished = true
    }

    override fun refreshAfterTokenDrawn() {
        accessedAfterTokenDrawn = true
    }

    override fun refreshBeforeTokenDiscard() {
        accessedBeforeTokenDiscard = true
    }

    override fun refreshAfterNobleVisit(nobleTile: NobleTile) {
        accessedAfterNobleVisit = true
    }

    override fun refreshBeforeNobleVisit(nobleTiles: List<NobleTile>) {
        accessedBeforeNobleVisit = true
    }

    override fun refreshAfterCardBought(card: DevelopmentCard, stack: SourceStack) {
        accessedAfterCardBought = true
    }

    override fun refreshAfterCardReserved(card: DevelopmentCard, source: SourceStack) {
        accessedAfterCardReserved = true
    }

    override fun refreshAfterTokenDiscard(tokens: List<Token>) {
        accessedAfterTokenDiscard = true
    }


}