package service

import entity.DevelopmentCard
import entity.Token
import entity.TokenMap


/**
 * Service class with methods to check if player moves are possible in general
 */
class CheckPlayerMove(val rootService: RootService) {

    /**
     * Checks if the current player can buy any card
     * @return true if the player can buy any card and false otherwise
     */
    fun buyCardPossible(): Boolean {
        val player = rootService.gameService.getCurrentPlayer()
        val game = rootService.currentGame.getCurrentGame()
        val jokerTokenCount = player.tokens[Token.GOLD]
        val playerTokensAndBonus = player.tokens.toMutableTokenMap().apply { this[Token.GOLD] = 0 }

        for (developmentCard in player.cards) {
            if (developmentCard.bonus != null) {
                playerTokensAndBonus[developmentCard.bonus] += 1
            }
        }
        for (i in 0 until game.openCards.size) {
            for (card in game.openCards[i]) {
                if (enoughTokens(card, jokerTokenCount, playerTokensAndBonus)) {
                    return true
                }
            }
        }
        for (card in player.reservedCards) {
            if (enoughTokens(card, jokerTokenCount, playerTokensAndBonus)) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the current play can select tokens.
     *
     *  If a token can be selected that is not gold, then the move is possible.
     *
     * @return true if the player can select Tokens
     */
    fun selectTokensPossible() =
        rootService.currentGame.getCurrentGame().tokens.all { (token, amt) -> token == Token.GOLD || amt > 0 }


    /**
     * checks if a reserve is possible
     * @return true if possible
     */
    fun reserveCardPossible(): Boolean {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        if (currentPlayer.reservedCards.size >= 3) return false

        game.openCards.forEach { if (it.isNotEmpty()) return true }
        game.drawCards.forEach { if (it.isNotEmpty()) return true }
        return false
    }

    /**
     * generally checks if a move is possible for the current player
     * @return true if possible
     */
    fun checkIfMovePossible() = reserveCardPossible() || buyCardPossible() || selectTokensPossible()

    private fun enoughTokens(card: DevelopmentCard, jokerTokenCount: Int, playerTokens: TokenMap): Boolean {
        var neededExtraTokens = 0

        for ((token, amt) in card.cost) {
            val diff = amt - playerTokens[token]
            if (diff > 0) {
                neededExtraTokens += diff
            }
        }
        if (jokerTokenCount >= neededExtraTokens) {
            return true
        }
        return false
    }
}