package entity

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.*

/**
 * Test class for [NobleCardImageLoader]
 */
class NobleCardImageLoaderTest {
    private val imageLoader: NobleCardImageLoader = NobleCardImageLoader()
    private var testNobleTile = NobleTile(mapOf(Token.ONYX to 4, Token.DIAMOND to 4).toTokenMap(), 9, 3)
    private var testNobleTileImage: BufferedImage? = null

    /**
     * Creates the image of the [testNobleTile]
     */
    @BeforeTest
    fun loadTestNobleTileImage() {
        val nobles: BufferedImage = ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/nobles.jpg"))
        val noble: BufferedImage = nobles.getSubimage(1620, 0, 180, 180)
        val gems: BufferedImage = ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/gems.png"))
        var onyxGem: BufferedImage = gems.getSubimage(0, 0, 90, 74)
        onyxGem = scaleImage(onyxGem, 0.25, 0.25)
        var diamondGem: BufferedImage = gems.getSubimage(180, 0, 90, 74)
        diamondGem = scaleImage(diamondGem, 0.25, 0.25)
        val brownNumbers: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/numbersBrown.png"))
        var brownNumberFour: BufferedImage = brownNumbers.getSubimage(1270, 625, 180, 240)
        brownNumberFour = scaleImage(brownNumberFour, 0.15, 0.15)
        val greyNumbers: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/numbersGrey.png"))
        var greyNumberFour: BufferedImage = greyNumbers.getSubimage(1270, 625, 180, 240)
        greyNumberFour = scaleImage(greyNumberFour, 0.15, 0.15)
        val whiteNumbers: BufferedImage =
            ImageIO.read(DevelopmentCardImageLoaderTest::class.java.getResource("/numbers.png"))
        var whiteNumberThree: BufferedImage = whiteNumbers.getSubimage(990, 625, 180, 240)
        whiteNumberThree = scaleImage(whiteNumberThree, 0.3, 0.25)
        val finalImage = BufferedImage(noble.width, noble.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = finalImage.createGraphics()
        graphics.drawImage(noble, 0, 0, null)
        graphics.drawImage(brownNumberFour, 5, 140, null)
        graphics.drawImage(onyxGem, 25, 140, null)
        graphics.drawImage(greyNumberFour, 5, 100, null)
        graphics.drawImage(diamondGem, 25, 100, null)
        graphics.drawImage(whiteNumberThree, 5, 3, null)
        testNobleTileImage = finalImage
    }

    /**
     * Tests if the image of the [testNobleTile] created with the createCompleteNobleImage function
     * is the same as the image created in [loadTestNobleTileImage]
     */
    @Test
    fun testCreateCompleteCardImageEquality() {
        val imageLoaderTestImage = imageLoader.createCompleteNobleImage(testNobleTile)
        assertTrue { imageLoaderTestImage sameAs testNobleTileImage }
    }

    /**
     * Tests if the image of a different NobleTile created with the createCompleteNobleImage function
     * is not the same as the image created in [loadTestNobleTileImage]
     */
    @Test
    fun testCreateCompleteCardImageInequality() {
        val imageLoaderTestImage =
            imageLoader.createCompleteNobleImage(NobleTile(mapOf(Token.SAPPHIRE to 9).toTokenMap(), 9, 3))
        assertFalse { imageLoaderTestImage sameAs testNobleTileImage }
    }

}

/**
 * Tests equality of two [BufferedImage]s by first checking if they have the same dimensions
 * and then comparing every pixels' RGB value.
 */
private infix fun BufferedImage.sameAs(other: Any?): Boolean {

    // if the other is not even a BufferedImage, we are done already
    if (other !is BufferedImage) {
        return false
    }

    // check dimensions
    if (this.width != other.width || this.height != other.height) {
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
