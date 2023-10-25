package entity

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.*

/**
 * Test class for [DevelopmentCardImageLoader]
 */
class DevelopmentCardImageLoaderTest {
    private val imageLoader: DevelopmentCardImageLoader = DevelopmentCardImageLoader()
    private var testDevelopmentCard = DevelopmentCard(mapOf(Token.RUBY to 3).toTokenMap(),
        Token.ONYX, 2, 2, 0)
    private var testDevelopmentCardImage: BufferedImage? = null

    /**
     * Creates the image of the [testDevelopmentCard]
     */
    @BeforeTest
    fun loadTestDevelopmentCardImage() {
        val cards: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/cards.jpg"))
        val cardBackground: BufferedImage = cards.getSubimage(0, 0, 230, 320)
        val gems: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/gems.png"))
        var rubyGem: BufferedImage = gems.getSubimage(360, 0, 90, 74)
        rubyGem = scaleImage(rubyGem, 0.5, 0.5)
        val numbers: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/numbers.png"))
        var costThree: BufferedImage = numbers.getSubimage(990, 75, 180, 240)
        costThree = scaleImage(costThree, 0.18, 0.18)
        val chips: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/chips.png"))
        var onyxChip: BufferedImage = chips.getSubimage(0, 0, 106, 106)
        onyxChip = scaleImage(onyxChip, 0.8, 0.8)
        var prestigePointsTwo: BufferedImage = numbers.getSubimage(710, 625, 180, 240)
        prestigePointsTwo = scaleImage(prestigePointsTwo, 0.35, 0.35)
        val finalImage = BufferedImage(cardBackground.width, cardBackground.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = finalImage.createGraphics()
        graphics.drawImage(cardBackground, 0, 0, null)
        graphics.drawImage(rubyGem, 10, 280, null)
        graphics.drawImage(costThree, 55, 280, null)
        graphics.drawImage(onyxChip, 135, 5, null)
        graphics.drawImage(prestigePointsTwo, 10, 10, null)
        testDevelopmentCardImage = finalImage
    }

    /**
     * Tests if the image of the [testDevelopmentCard] created with the createCompleteCardImage function
     * is the same as the image created in [loadTestDevelopmentCardImage]
     */
    @Test
    fun testCreateCompleteCardImageEquality() {
        val imageLoaderTestImage = imageLoader.createCompleteCardImage(testDevelopmentCard)
        assertTrue { imageLoaderTestImage sameAs testDevelopmentCardImage }
    }

    /**
     * Tests if the image of a different DevelopmentCard created with the createCompleteCardImage function
     * is not the same as the image created in [loadTestDevelopmentCardImage]
     */
    @Test
    fun testCreateCompleteCardImageInequality() {
        val imageLoaderTestImage =
            imageLoader.createCompleteCardImage(DevelopmentCard(mapOf(Token.SAPPHIRE to 9).toTokenMap(),
                Token.DIAMOND, 3, 2, 0))
        assertFalse { imageLoaderTestImage sameAs testDevelopmentCardImage }
    }

    /**
     * Tests if the creation of an already existing CardImage works as intended
     */
    @Test
    fun testLoadingExistingCardImage(){
        val imageLoaderTestImage = imageLoader.createCompleteCardImage(testDevelopmentCard)
        val imageLoaderSecondTestImage= imageLoader.createCompleteCardImage(testDevelopmentCard)
        assertTrue { imageLoaderSecondTestImage sameAs imageLoaderTestImage }
    }

    /**
     * Tests if the backImages created with the backImageForTier function are correct
     */
    @Test
    fun testBackImageForTier() {
        val tiersImage: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/deck.png"))
        val tierOne = tiersImage.getSubimage(0, 0, 230, 320)
        val tierTwo = tiersImage.getSubimage(230, 0, 230, 320)
        val tierThree = tiersImage.getSubimage(460, 0, 230, 320)
        assertTrue { imageLoader.backImageForTier(1) sameAs tierOne }
        assertTrue { imageLoader.backImageForTier(2) sameAs tierTwo }
        assertTrue { imageLoader.backImageForTier(3) sameAs tierThree }

    }

}

/**
 * Tests equality of two [BufferedImage]s by first checking if they have the same dimensions
 * and then comparing every pixels' RGB value.
 */
private infix fun BufferedImage.sameAs(other: Any?): Boolean {

    // if the other is not even a BufferedImage or the dimensions are wrong, we are done already
    if (other !is BufferedImage || this.width != other.width || this.height != other.height) {
        return false
    }
     // compare every pixel
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (this.getRGB(x, y) != other.getRGB(x, y))
                return false
        }
    }

    // if we reach this point, dimensions and pixels match
    return true

}
