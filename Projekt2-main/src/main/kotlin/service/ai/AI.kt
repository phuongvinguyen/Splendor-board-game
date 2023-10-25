package service.ai

import entity.*
import service.SourceStack
import service.ai.moves.Move

/**
 * This interface defines common methods of a Splendor game AI.
 */
interface AI {
    /**
     * Determines and returns the next best move to take depending on the
     * current [gameState] and based on this AI's difficulty. See the
     * respective AI's implementation of this method to learn about its
     * decision-making.
     * @param gameState the current game state based upon which this
     * AI should choose the best possible move
     * @return the best move based on the current game state
     */
    fun determineBestMove(gameState: GameState, maxTimeMs: Long): Move

    /**
     * Finds all nobles that may visit a player, after applying the extraBoni
     *
     * @param game The game state
     * @param player The player
     * @param extraBonuses The extra boni to apply
     * @return A list of all available noble tiles
     */
    fun getAvailablePlayerNobles(game: GameState, player: Player, extraBonuses: List<Token>): List<NobleTile> {
        val playerBonuses = MutableTokenMap()
        for (card in player.cards) {
            if (card.bonus != null) {
                playerBonuses[card.bonus] += 1
            }
        }
        for (extra in extraBonuses) {
            playerBonuses[extra] += 1
        }
        return getAvailableNobles(game, playerBonuses)
    }

    /**
     * This method returns all noble tiles that are available given some specific boni
     *
     * @param game The game state
     * @param boni The boni that are available
     * @return A list of all open noble tiles that are available
     */
    fun getAvailableNobles(game: GameState, boni: TokenMap) = game.nobleTiles.filter { noble ->
        noble.requirements.all { (token, amount) -> boni[token] >= amount }
    }

    /**
     * This method returns all cards that can potentially be reserved
     *
     * @param game The game state
     * @return A list of all cards that could be reserved and their stack
     */
    fun getAvailableReserveCards(game: GameState): List<Pair<DevelopmentCard, SourceStack>> {
        val cards = mutableListOf<Pair<DevelopmentCard, SourceStack>>()

        for (it in game.openCards.withIndex()) {
            val stack = it.value
            for (card in stack) {
                cards.add(card to SourceStack.open(it.index + 1))
            }
        }

        for (it in game.drawCards.withIndex()) {
            val stack = it.value
            val lastCard = stack.lastOrNull()
            if (lastCard != null) {
                cards.add(lastCard to SourceStack.draw(it.index + 1))
            }
        }

        return cards
    }

    /**
     * the Method returns the minimum payment a player needs to pay for the giving card
     * in regards to his tokens and cards
     * @param player, the player for which the method is called
     * @param card, the card the player is interested in purchasing
     * @return the minimum payment needed by the [player] to buy the [card]
     */
    fun getMinimumPayment(player: Player, card: DevelopmentCard): TokenMap {
        val payment = card.cost.toMutableTokenMap()
        removePlayerBoni(player, payment)

        // pay with gold if required
        for ((token, amount) in payment) {
            val playerAmount = player.tokens[token]
            val diff = amount - playerAmount
            if (diff > 0) {
                payment[token] = playerAmount
                payment[Token.GOLD] += diff
            }
        }

        return payment
    }

    /**
     * apply the Bonus from the player's Development cards
     * @param player, the player with the bonuses to be calculated
     * @param tokens, the tokens that the player would like to add the bonus to
     */
    private fun removePlayerBoni(player: Player, tokens: MutableTokenMap) {
        for (playerCard in player.cards) {
            if (playerCard.bonus == null) {
                continue
            }
            val specificBonus = tokens[playerCard.bonus]
            if (specificBonus > 0) {
                tokens -= playerCard.bonus
            }
        }
    }

    /**
     * This method returns a list of all cards that the player can afford
     *
     * @param game The game state
     * @param player: The player who should pay
     * @return A list of cards and their stack
     */
    fun getAffordableCards(game: GameState, player: Player): List<Pair<DevelopmentCard, SourceStack>> {
        val totalTokens = getTotalPlayerTokens(player)

        val affordableCards = mutableListOf<Pair<DevelopmentCard, SourceStack>>()
        for (cardStack in game.openCards.withIndex()) {
            for (card in cardStack.value) {
                if (isCardAffordable(card, player, totalTokens)) {
                    affordableCards.add(card to SourceStack.open(cardStack.index + 1))
                }
            }
        }

        for (reservedCard in player.reservedCards) {
            if (isCardAffordable(reservedCard, player, totalTokens)) {
                affordableCards.add(reservedCard to SourceStack.RESERVED)
            }
        }

        return affordableCards
    }

    /**
     * This method returns whether a card is affordable
     *
     * @param card The card to check
     * @param totalTokens The total amount of tokens a player can pay
     * @return whether the card is affordable by player
     */
    fun isCardAffordable(card: DevelopmentCard, player: Player, totalTokens: TokenMap): Boolean {
        // how many more tokens would the player need to buy this card?
        var tokenDiff = 0
        for ((token, amount) in card.cost) {
            val diff = amount - totalTokens[token]
            if (diff > 0) {
                tokenDiff += diff
            }
        }
        return player.tokens[Token.GOLD] >= tokenDiff
    }

    /**
     * This method returns the total number of tokens a player can buy, including bonuses
     */
    fun getTotalPlayerTokens(player: Player): MutableTokenMap {
        val totalTokens = player.tokens.toMutableTokenMap()
        totalTokens[Token.GOLD] = 0
        for (playerCard in player.cards) {
            if (playerCard.bonus != null) {
                totalTokens[playerCard.bonus] += 1
            }
        }
        return totalTokens
    }


    companion object {
        val TOKEN_COMBINATIONS = arrayOf(
            arrayOf(Token.EMERALD, Token.RUBY, Token.SAPPHIRE),
            arrayOf(Token.EMERALD, Token.RUBY, Token.DIAMOND),
            arrayOf(Token.EMERALD, Token.RUBY, Token.ONYX),
            arrayOf(Token.EMERALD, Token.SAPPHIRE, Token.DIAMOND),
            arrayOf(Token.EMERALD, Token.SAPPHIRE, Token.ONYX),
            arrayOf(Token.EMERALD, Token.DIAMOND, Token.ONYX),
            arrayOf(Token.RUBY, Token.SAPPHIRE, Token.DIAMOND),
            arrayOf(Token.RUBY, Token.SAPPHIRE, Token.ONYX),
            arrayOf(Token.RUBY, Token.DIAMOND, Token.ONYX),
            arrayOf(Token.SAPPHIRE, Token.DIAMOND, Token.ONYX),
        )
    }
}