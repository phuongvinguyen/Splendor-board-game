package entity


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test class for [ScoreboardEntry]
 */
class ScoreboardEntryTest {
    /**
     * The compareTo function is tested with multiple ScoreboardEntries
     */
    @Test
    fun testCompareTo() {
        var firstScoreboardEntry = ScoreboardEntry("Test1", 16, 3, 10)
        var secondScoreboardEntry = ScoreboardEntry("Test2", 15, 6, 7)
        assertTrue { firstScoreboardEntry > secondScoreboardEntry }

        firstScoreboardEntry = ScoreboardEntry("Test1", 15, 3, 10)
        secondScoreboardEntry = ScoreboardEntry("Test2", 15, 6, 7)
        assertTrue { firstScoreboardEntry > secondScoreboardEntry }

        firstScoreboardEntry = ScoreboardEntry("Test1", 15, 6, 10)
        secondScoreboardEntry = ScoreboardEntry("Test2", 15, 6, 7)
        assertTrue { firstScoreboardEntry < secondScoreboardEntry }

        firstScoreboardEntry = ScoreboardEntry("Test1", 15, 6, 7)
        secondScoreboardEntry = ScoreboardEntry("Test2", 15, 6, 7)
        assertTrue { firstScoreboardEntry.compareTo(secondScoreboardEntry) == 0 }

    }

    /**
     * The scoreboardEntry is encoded into a String then decoded and the equality between the original scoreboardEntry
     * and the decoded scoreboardEntry is tested
     */
    @Test
    fun encodeAndDecodeScoreboard() {
        val scoreboardEntry = ScoreboardEntry("Test1", 16, 3, 10)
        val scoreboardJson = Json.encodeToString(scoreboardEntry)
        val loadedEntry: ScoreboardEntry = Json.decodeFromString(scoreboardJson)
        assertEquals(scoreboardEntry.name, loadedEntry.name)
        assertEquals(scoreboardEntry.score, loadedEntry.score)
        assertEquals(scoreboardEntry.developmentCards, loadedEntry.developmentCards)
        assertEquals(scoreboardEntry.numRounds, loadedEntry.numRounds)
    }

    /**
     * Tests if setNumRounds works as intended
     */
    @Test
    fun testSetNumRounds() {
        val scoreboardEntry = ScoreboardEntry("Test1", 16, 3, 10)
        scoreboardEntry.numRounds = 3
        assertEquals(3, scoreboardEntry.numRounds)
    }
}