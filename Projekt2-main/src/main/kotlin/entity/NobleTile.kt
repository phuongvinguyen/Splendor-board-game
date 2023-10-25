package entity

import kotlinx.serialization.Serializable

/**
 * Entity class representing a noble tile
 *
 * @param requirements specifies the requirements needed for a visit
 * @param imageID ID of the Image associated with this noble tile
 * @param prestigePoints Number of prestige points the card gives to the player
 */

@Serializable
data class NobleTile(
    val requirements: TokenMap,
    val imageID: Int,
    val prestigePoints: Int
) {
    /**
     * Creates an easier to read String for the GUI
     */
    fun easierReadableString(): String {
        val prestigePointsText = if (prestigePoints == 1) {
            "prestige point"
        } else {
            "prestige points"
        }
        var requirementString = ""
        for ((token, amount) in requirements) {
            if (amount > 0)
                requirementString += " $amount ${if (amount == 1) token else "${token}s"}"
        }
        return "gives $prestigePoints $prestigePointsText and requires$requirementString"
    }

}