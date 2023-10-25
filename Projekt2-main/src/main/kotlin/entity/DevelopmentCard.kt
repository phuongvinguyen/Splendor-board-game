package entity

import kotlinx.serialization.Serializable

/**
 * Entity class representing a development card
 *
 * @param cost Map of tokens and its quantity to buy this card
 * @param bonus specifies the bonus of the card
 * @param tier Int which specifies the level/tier of this development card. (three tiers)
 * @param imageID ID of the Image associated with this noble tile
 * @param prestigePoints Number of prestige points the card gives to the player
 */
@Serializable
data class DevelopmentCard(
    val cost: TokenMap,
    val bonus: Token?,
    val prestigePoints: Int,
    val tier: Int,
    val imageID: Int
) {
    override fun toString() = "[${prestigePoints} P., bonus ${bonus}, cost: ${cost}]"

    /**
     * Creates an easier to read String for the GUI
     */
    fun easierReadableString(): String {
        val prestigePointsText = if (prestigePoints == 1) {
            "prestige point"
        } else {
            "prestige points"
        }
        val indefiniteArticle = if (bonus == Token.ONYX) {
            "an"
        } else {
            "a"
        }

        val smallerCostMap = cost.filter { (_, amt) -> amt != 0 }
        var costString = ""
        for ((token, amt) in smallerCostMap) {
            costString += " $amt ${if (amt == 1) token else "${token}s"}"
        }
        return "gives $prestigePoints $prestigePointsText, has $indefiniteArticle ${bonus.toString()} bonus " +
                "and costs$costString"
    }
}