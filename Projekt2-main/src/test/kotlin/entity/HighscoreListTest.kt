package entity

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

import kotlin.test.assertEquals
/**
 * Test class for [HighscoreList]
 */
class HighscoreListTest {
    /**
     * A highscoreList is encoded into a String then decoded and the equality between the original highscoreList
     * and the decoded highscoreList is tested
     */
    @Test
    fun encodeAndDecodeHighscoreList() {
        val highscoreList=HighscoreList()
        highscoreList.scores.add(ScoreboardEntry("Test1",2,4,1))
        val highscoreListJson = Json.encodeToString(highscoreList)
        val loadedhighscoreList: HighscoreList = Json.decodeFromString(highscoreListJson)
        assertEquals(highscoreList,loadedhighscoreList)
    }
}