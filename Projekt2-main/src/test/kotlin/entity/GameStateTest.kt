package entity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test the Game State Entity
 */
class GameStateTest {

    val players = listOf(Player("Eros", PlayerType.HUMAN), Player("Steven", PlayerType.EASY))
    val game = GameState(players)

    /**
     * test if the copy is an Exact copy
     */
    @Test
    fun copyTest() {
        val gameCopy = game.copy()
        assertEquals(game, gameCopy)
    }

    /**
     * A gameState is encoded into a String then decoded and the equality between the original gameState
     * and the decoded gameState is tested
     */
    @Test
    fun encodeAndDecodeGameState() {
        val gameStateJson = Json.encodeToString(game)
        val loadedGameState: GameState = Json.decodeFromString(gameStateJson)
        assertEquals(game,loadedGameState)
    }


}