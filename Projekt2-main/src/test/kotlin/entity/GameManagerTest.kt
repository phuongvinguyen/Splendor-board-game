package entity

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
/**
 * Test class for [GameManager]
 */
class GameManagerTest {
    /**
     * A gameManager is encoded into a String then decoded and the equality between the original gameManager
     * and the decoded gameManager is tested
     */
    @Test
    fun encodeAndDecodeGameManager() {
        val gameManager=GameManager(currentGameIndex = 4, allowHighscore = false, currentState = GameState())
        val gameManagerJson = Json.encodeToString(gameManager)
        val loadedGameManager: GameManager = Json.decodeFromString(gameManagerJson)
        assertEquals(gameManager,loadedGameManager)
    }
}