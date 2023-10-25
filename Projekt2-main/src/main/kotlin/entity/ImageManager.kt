package entity

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 *This class represents the images of cards
 *
 */
private const val GEMS_FILE = "/gems.png"

/**
 * Methode to adjust the ratio of cards
 *
 */
fun scaleImage(imageToScale: BufferedImage, scaleX: Double, scaleY: Double): BufferedImage {
    val w = imageToScale.width
    val h = imageToScale.height
    var after = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val at = AffineTransform()
    at.scale(scaleX, scaleY)
    val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
    after = scaleOp.filter(imageToScale, after)
    return after
}
/**
 * Methode to adjust the back-and foreground of the image
 *
 */
fun overlayImage(
    imageBackground: BufferedImage, imageForeground: BufferedImage, posX: Int, posY: Int
): BufferedImage {
    val imgW = imageBackground.width
    val imgH = imageBackground.height
    val overlay = BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB)
    val g = overlay.createGraphics()
    g.drawImage(imageBackground, 0, 0, null)
    g.drawImage(imageForeground, posX, posY, null)
    return overlay
}

/**
 * Methode to show the cost of the cards
 */
fun loadWhiteNumbers(path: String): List<BufferedImage> {
    val numbersSeperated = mutableListOf<BufferedImage>()
    val allNumbers = ImageIO.read(ImageManager::class.java.getResource((path)))
    for (i in 0..4) {
        numbersSeperated.add(allNumbers.getSubimage(150 + i * 280, 625, 180, 240))
    }
    for (i in 0..4) {
        numbersSeperated.add(allNumbers.getSubimage(150 + i * 280, 900, 180, 240))
    }
    return numbersSeperated
}

/**
 * Methode to show the gems
 */
fun loadGems(): List<BufferedImage> {
    val gemsSeperated = mutableListOf<BufferedImage>()
    val allGems = ImageIO.read(DevelopmentCardImageLoader::class.java.getResource(GEMS_FILE))
    for (i in 0..4) {
        gemsSeperated.add(allGems.getSubimage(0 + i * 90, 0, 90, 74))
    }
    return gemsSeperated
}

/**
 * a method to manage the gems in the game
 */
fun getGemsMap(): Map<Token, BufferedImage> {
    val gems = loadGems()
    return mapOf(
        Token.ONYX to gems[0],
        Token.SAPPHIRE to gems[1],
        Token.DIAMOND to gems[2],
        Token.EMERALD to gems[3],
        Token.RUBY to gems[4]
    )
}

/**
 * This Class is in charge of assigning development cards the correct labels,
 * such as gems price and points and laying them over the card, so they are visible
 */
class ImageManager