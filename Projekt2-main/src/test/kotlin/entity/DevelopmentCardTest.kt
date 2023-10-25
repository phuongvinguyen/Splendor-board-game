package entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test the Development cards Entity
 */
class DevelopmentCardTest {

    val card = DevelopmentCard(
        cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
        bonus = Token.ONYX, prestigePoints = 0, tier = 1, imageID = 1
    )

    val cardDiscription = card.toString()

    /**
     * test if the card description is correct
     */
    @Test
    fun test() {
        print(cardDiscription)
    }


    /**
     * test if the correct image id is returned
     */
    @Test
    fun getImageIDTest() {
        val imageID = card.imageID
        assertEquals(1, imageID)
    }
    /**
     * Tests if the easierReadableString function works as intended
     */
    @Test
    fun easierReadableStringTest() {
        val secondCard=DevelopmentCard(
        cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
        bonus = Token.DIAMOND, prestigePoints = 1, tier = 1, imageID = 1)
        var expectedString = "gives 0 prestige points, has an Onyx bonus and costs 1 Sapphire 1 Diamond 1 Emerald"
        assertEquals(expectedString, card.easierReadableString())
        expectedString="gives 1 prestige point, has a Diamond bonus and costs 1 Sapphire 1 Diamond 1 Emerald"
        assertEquals(expectedString, secondCard.easierReadableString())
        val thirdCard=DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.ONYX, prestigePoints = 1, tier = 1, imageID = 1)
        expectedString="gives 1 prestige point, has an Onyx bonus and costs 1 Sapphire 1 Diamond 1 Emerald"
        assertEquals(expectedString, thirdCard.easierReadableString())
        val fourthCard=DevelopmentCard(
            cost = mapOf(Token.DIAMOND to 1, Token.SAPPHIRE to 1, Token.EMERALD to 1).toTokenMap(),
            bonus = Token.DIAMOND, prestigePoints = 2, tier = 1, imageID = 1)
        expectedString="gives 2 prestige points, has a Diamond bonus and costs 1 Sapphire 1 Diamond 1 Emerald"
        assertEquals(expectedString, fourthCard.easierReadableString())
    }
}