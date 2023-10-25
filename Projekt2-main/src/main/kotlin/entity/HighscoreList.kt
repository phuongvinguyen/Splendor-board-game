package entity

import kotlinx.serialization.Serializable

/**
 * Entity class HighscoreList is a highscore consisting of a mutableList containing ScoreboardEntries
 */

@Serializable
data class HighscoreList(val scores: MutableList<ScoreboardEntry> =mutableListOf())