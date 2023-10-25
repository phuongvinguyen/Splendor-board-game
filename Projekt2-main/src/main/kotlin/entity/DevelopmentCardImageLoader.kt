package entity

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private const val FRONT_CARDS_FILE = "/cards.jpg"
private const val BACK_CARDS_FILE = "/deck.png"
private const val NUMBERS_FILE = "/numbers.png"

private const val CHIPS_FILE = "/chips.png"
private const val IMG_WIDTH = 230
private const val IMG_HEIGHT = 320





/**
 * Provides access to the src/main/resources/cards.jpg file that contains all development
 * card images in a raster. The returned [BufferedImage] objects of [frontImageFor],
 * and [backImageForTier] are 230x320 pixels.
 */
class DevelopmentCardImageLoader {
    private val completeCardImages: HashMap<DevelopmentCard,BufferedImage> = hashMapOf()
    private val gemsSeperated = loadGems()
    private val whiteNumbersSeperated = loadWhiteNumbers(NUMBERS_FILE)
    private val blackNumbersSeperated = loadBlackNumbers()
    private val chipsSeperated = loadChips()
    /**
     * The full raster image containing the development card's picture as rows.
     */
    private val frontImage: BufferedImage =
        ImageIO.read(DevelopmentCardImageLoader::class.java.getResource(FRONT_CARDS_FILE))
    private val backImage: BufferedImage =
        ImageIO.read(DevelopmentCardImageLoader::class.java.getResource(BACK_CARDS_FILE))

    /**
     * Create the complete cards image
     */
    fun createCompleteCardImage(card: DevelopmentCard): BufferedImage {
        if(completeCardImages.contains(card)){
            return completeCardImages[card]!!
        }
        val backgroundImage = frontImageFor(card.imageID)
        val cardImageWithCost = addCostToCardImage(card, backgroundImage)
        val cardImageWithCostAndBonus = addBonusToCardImage(card, cardImageWithCost)
        val i =  addPrestigeToCardImage(card, cardImageWithCostAndBonus)
        completeCardImages[card] = i
        return i
    }

    /**
     * Provides the card image for the given [imageID].
     */
    fun frontImageFor(imageID: Int) =
        getFrontImageByCoordinates(imageID)

    /**
     * Provides the card (back) image for the given [tier], starting at
     * 1 up to and including 3.
     */
    fun backImageForTier(tier: Int) =
        getBackImageByCoordinates(tier - 1)


    /**
     * retrieves from the full raster front face image [frontImage] the corresponding
     * sub-image for the given column [x]
     *
     * @param x column in the raster image, starting at 0
     *
     */
    private fun getFrontImageByCoordinates(x: Int): BufferedImage =
        frontImage.getSubimage(
            x * IMG_WIDTH,
            0,
            IMG_WIDTH,
            IMG_HEIGHT
        )

    /**
     * retrieves from the full raster back face image [backImage] the corresponding
     * sub-image for the given column [x]
     *
     * @param x column in the raster image, starting at 0
     *
     */
    private fun getBackImageByCoordinates(x: Int): BufferedImage =
        backImage.getSubimage(
            x * IMG_WIDTH,
            0,
            IMG_WIDTH,
            IMG_HEIGHT
        )

    private fun addBonusToCardImage(card: DevelopmentCard, bufferedImage: BufferedImage): BufferedImage {
        checkNotNull(card.bonus)
        val chipsSeperated = chipsSeperated.toMutableList()
        chipsSeperated.replaceAll { scaleImage(it, 0.8, 0.8) }
        val bonusImage = when (card.bonus) {
            Token.DIAMOND -> chipsSeperated[2]
            Token.EMERALD -> chipsSeperated[3]
            Token.SAPPHIRE -> chipsSeperated[1]
            Token.ONYX -> chipsSeperated[0]
            Token.RUBY -> chipsSeperated[4]
            else -> throw IllegalArgumentException("Invalid card")
        }
        return overlayImage(bufferedImage, bonusImage, 135, 5)
    }

    private fun addPrestigeToCardImage(card: DevelopmentCard, bufferedImage: BufferedImage): BufferedImage {
        if(card.prestigePoints == 0) return bufferedImage
        val numbersSeperated = whiteNumbersSeperated.toMutableList()
        numbersSeperated.replaceAll{ scaleImage(it, 0.35, 0.35) }
        return overlayImage(bufferedImage, numbersSeperated[card.prestigePoints], 10, 10)
    }



    private fun addCostToCardImage(card: DevelopmentCard, bufferedImage: BufferedImage): BufferedImage {
        val gemsSeperated = gemsSeperated.toMutableList()
        val numbersSeperated = blackNumbersSeperated.toMutableList()
        gemsSeperated.replaceAll { scaleImage(it, 0.5, 0.5) }
        numbersSeperated.replaceAll { scaleImage(it, 0.18, 0.18) }
        var index = 0
        var currentImage = bufferedImage
        card.cost.forEach { (token, amt) ->
            if (amt == 0) return@forEach
            require(amt in 0..9) { "Bonus not displayable" }
            val tokenImage = gemsSeperated[token.ordinal]
            val numberImage = numbersSeperated[amt]

            currentImage = overlayImage(currentImage, tokenImage, 10, 280 - index * 40)
            currentImage = overlayImage(currentImage, numberImage, 55, 280 - index * 40)
            index++
        }
        return currentImage
    }




    private fun loadBlackNumbers(): List<BufferedImage> {
        val numbersSeperated = mutableListOf<BufferedImage>()
        val allNumbers = ImageIO.read(DevelopmentCardImageLoader::class.java.getResource(NUMBERS_FILE))
        for (i in 0..4) {
            numbersSeperated.add(allNumbers.getSubimage(150 + i * 280, 75, 180, 240))
        }
        for (i in 0..4) {
            numbersSeperated.add(allNumbers.getSubimage(150 + i * 280, 350, 180, 240))
        }
        return numbersSeperated
    }

    private fun loadChips(): List<BufferedImage> {
        var offset = 0
        val chipsSeperated = mutableListOf<BufferedImage>()
        val allChips = ImageIO.read(DevelopmentCardImageLoader::class.java.getResource(CHIPS_FILE))
        for(i in 1..Token.values().size){
            chipsSeperated.add(allChips.getSubimage(offset, 0, 106, 106))
            offset += 106
            if (offset == 212) offset += 1
            if (offset == 319) offset += 1
            if (offset == 426) offset += 1
        }
        return chipsSeperated
    }
}