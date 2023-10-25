package entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
/**
 * Test class for [NobleTile]
 */
class NobleTileTest {
    private val firstNobleTile = NobleTile(mapOf(Token.DIAMOND to 2).toTokenMap(), 0, 3)
    private val secondNobleTile = NobleTile(mapOf(Token.SAPPHIRE to 1).toTokenMap(), 0, 1)

    /**
     * Tests the equals function
     */
    @Test
    fun testEquals() {
        assertEquals(firstNobleTile, NobleTile(mapOf(Token.DIAMOND to 2).toTokenMap(), 0, 3))
        assertNotEquals(firstNobleTile, secondNobleTile)
    }

    /**
     * Tests if the easierReadableString function works as intended
     */
    @Test
    fun easierReadableStringTest() {
        var expectedString = "gives 3 prestige points and requires 2 Diamonds"
        assertEquals(expectedString, firstNobleTile.easierReadableString())
        expectedString = "gives 1 prestige point and requires 1 Sapphire"
        assertEquals(expectedString, secondNobleTile.easierReadableString())
    }

}