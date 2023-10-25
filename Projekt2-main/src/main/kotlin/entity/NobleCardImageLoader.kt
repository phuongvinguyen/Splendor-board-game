package entity

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private const val NOBLE_FRONT_FILE = "/nobles.jpg"
private const val NUMBERS_FILE = "/numbers.png"
private const val NUMBERS_GREEN_FILE = "/numbersGreen.png"
private const val NUMBERS_RED_FILE = "/numbersRed.png"
private const val NUMBERS_BROWN_FILE = "/numbersBrown.png"
private const val NUMBERS_BLUE_FILE = "/numbersBlue.png"
private const val NUMBERS_GREY_FILE = "/numbersGrey.png"

private const val IMG_WIDTH = 180
private const val IMG_HEIGHT = 180

/**
 * class NobleCardImageLoader describes the images of card from noble tiles
 */
class NobleCardImageLoader {

    private val redNumbersSeperated = loadWhiteNumbers(NUMBERS_RED_FILE)
    private val greenNumbersSeperated = loadWhiteNumbers(NUMBERS_GREEN_FILE)
    private val blueNumbersSeperated = loadWhiteNumbers(NUMBERS_BLUE_FILE)
    private val brownNumbersSeperated = loadWhiteNumbers(NUMBERS_BROWN_FILE)
    private val greyNumbersSeperated = loadWhiteNumbers(NUMBERS_GREY_FILE)
    private val gemsSeperated = loadGems()
    private val numbersSeperated = loadWhiteNumbers(NUMBERS_FILE)

    private val redNumbersSeperatedScaled =
        redNumbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.15, 0.15) } }
    private val greenNumbersSeperatedScaled =
        greenNumbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.15, 0.15) } }
    private val blueNumbersSeperatedScaled =
        blueNumbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.15, 0.15) } }
    private val brownNumbersSeperatedScaled =
        brownNumbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.15, 0.15) } }
    private val greyNumbersSeperatedScaled =
        greyNumbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.15, 0.15) } }
    private val gemsSeperatedScaled = gemsSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.25, 0.25) } }
    private val numbersSeperatedScaled = numbersSeperated.toMutableList().apply { replaceAll { scaleImage(it, 0.3, 0.25) } }
    /**
     * Provides access to the src/main/resources/nobles.jpg file that contains all development
     * card images in a raster. The returned [BufferedImage] objects of [frontImageForNobles] are 230x320 pixels.
     */

    /**
     * The full raster image containing the development card's picture as rows.
     */
    private val nobleFrontImage: BufferedImage =
        ImageIO.read(DevelopmentCardImageLoader::class.java.getResource((NOBLE_FRONT_FILE)))

    /**
     * The methode to show the images of noble cards
     */
    fun createCompleteNobleImage(nobleTile: NobleTile): BufferedImage {
        val backgroundImage = frontImageForNobles(nobleTile.imageID)
        val nobleImageWithCost = addCostToNobleImage(nobleTile, backgroundImage)
        return addPrestigeToNobleImage(nobleTile, nobleImageWithCost)
    }

    /**
     * Methode to show the prestige points on noble cards
     */
    private fun addPrestigeToNobleImage(nobleTile: NobleTile, bufferedImage: BufferedImage): BufferedImage {
        if(nobleTile.prestigePoints == 0) return bufferedImage
        return overlayImage(bufferedImage, numbersSeperatedScaled[nobleTile.prestigePoints], 5, 3)
    }

    /**
     * Methode to show the cost of noble cards
     */
    private fun addCostToNobleImage(nobleTile: NobleTile, bufferedImage: BufferedImage): BufferedImage {
        var index = 0
        var currentImage = bufferedImage
        nobleTile.requirements.forEach { (k, v) ->
            require(v in 0..9) { "Requirement not displayable" }
            if (v == 0) return@forEach
            val coloredNumber = when (k) {
                Token.RUBY -> redNumbersSeperatedScaled[v]
                Token.ONYX -> brownNumbersSeperatedScaled[v]
                Token.SAPPHIRE -> blueNumbersSeperatedScaled[v]
                Token.EMERALD -> greenNumbersSeperatedScaled[v]
                Token.DIAMOND -> greyNumbersSeperatedScaled[v]
                else -> throw IllegalArgumentException("Invalid noble tile")
            }

            val gem = when (k) {
                Token.RUBY -> gemsSeperatedScaled[4]
                Token.ONYX -> gemsSeperatedScaled[0]
                Token.SAPPHIRE -> gemsSeperatedScaled[1]
                Token.EMERALD -> gemsSeperatedScaled[3]
                Token.DIAMOND -> gemsSeperatedScaled[2]
                else -> throw IllegalArgumentException("Invalid noble tile")
            }
            currentImage = overlayImage(currentImage, coloredNumber, 5, 140 - index * 40)
            currentImage = overlayImage(currentImage, gem, 25, 140 - index * 40)
            index++
        }
        return currentImage
    }

    /**
     * * Provides the noble image for the given [imageID].
     */
    fun frontImageForNobles(imageID: Int) =
        getFrontImageByCoordinatesForNoble(imageID)


    /**
     * retrieves from the full raster front face image [nobleFrontImage] the corresponding
     * sub-image for the given column [x]
     *
     * @param x column in the raster image, starting at 0
     *
     */

    private fun getFrontImageByCoordinatesForNoble(x: Int): BufferedImage =
        nobleFrontImage.getSubimage(
            x * IMG_WIDTH,
            0,
            IMG_WIDTH,
            IMG_HEIGHT
        )


}