package entity

import kotlinx.serialization.Serializable

/**
 * This class represents a human or non-human player for this game
 * and stores the players assets
 *
 * @param name is the name of the player
 * @param playerType  Distinguishes between human and bot (and their difficulty levels)
 * @param tokens are a Map of tokens and its quantity that belong to the player
 * @param allowHighscore Boolean. Is set false if the player asks for a hint (otherwise true)
 * @param cards List of development cards belong to the player
 * @param nobleTiles List of noble tile belong to the player
 */
@Serializable
data class Player(
    val name: String,
    val playerType: PlayerType,
    var tokens: TokenMap = MutableTokenMap(),
    var allowHighscore: Boolean = true,
    var cards: List<DevelopmentCard> = emptyList(),
    var reservedCards: List<DevelopmentCard> = emptyList(),
    var nobleTiles: List<NobleTile> = emptyList()
) {

    /**
     * Calculates the sum of tokens of the player
     */

    fun totalTokens() = tokens.sumOf { (_, amt) -> amt }
}