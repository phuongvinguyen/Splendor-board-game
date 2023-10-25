package entity

import kotlinx.serialization.Serializable

/**
 * Entity class to manage scoreboard entries.
 *
 * @param name Name of the player to be entered in the scoreboard
 * @param score Number of prestige points achieved by the player
 * @param developmentCards Number of Development Cards possessed by the player at the time of victory (less = better).
 * @param numRounds Rounds played until the victory is achieved
 */

@Serializable
data class ScoreboardEntry(
    val name: String = "", val score: Int = 0, val developmentCards: Int = 0, var numRounds: Int = 0
) : Comparable<ScoreboardEntry> {

    // Higher score is better as well as a low quantity of developmentCards and numRounds
    override fun compareTo(other: ScoreboardEntry) = compareBy<ScoreboardEntry>(
        {it.score},
        {-it.developmentCards},
        {-it.numRounds}
    ).compare(this, other)
}