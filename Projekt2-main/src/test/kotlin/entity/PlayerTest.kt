package entity

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test the Player Entity
 */
class PlayerTest {
    private val tokens = mapOf(Token.ONYX to 3, Token.RUBY to 1).toTokenMap()
    private val cards = listOf(DevelopmentCard(mapOf(Token.ONYX to 2).toTokenMap(), Token.EMERALD, 2, 1, 0))
    private val rCards = listOf(DevelopmentCard(mapOf(Token.SAPPHIRE to 2).toTokenMap(), Token.ONYX, 7, 1, 0))
    private val nobles = listOf(NobleTile(mapOf(Token.SAPPHIRE to 2).toTokenMap(), 0, 3))
    private val player = Player("Eros", PlayerType.HUMAN, tokens, false, cards, rCards, nobles)

    /**
     * test if the correct number of total tokens is returned
     */
    @Test
    fun totalTokensTest() {
        val tokens = player.totalTokens()
        assertEquals(4, tokens)
    }

    /**
     * test if allow high Score State could be changed
     */
    @Test
    fun setAllowHighScoreTest() {
        player.allowHighscore = true
        assertTrue { player.allowHighscore }
    }

    /**
     * The player is encoded into a String then decoded and the equality between the original player
     * and the decoded player is tested
     */
    @Test
    fun encodeAndDecodePlayer() {
        val playerJson = Json.encodeToString(player)
        val loadedPlayer: Player = Json.decodeFromString(playerJson)
        assertEquals(player,loadedPlayer)
    }
}