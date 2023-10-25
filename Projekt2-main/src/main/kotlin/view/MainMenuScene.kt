package view

import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.Dialog
import tools.aqua.bgw.dialog.DialogType
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.Desktop
import java.net.URI

/**
 * This class describes the main menu scene, where the player can start game, load saved game, see highscore list
 * read tutorials or quit
 */
class MainMenuScene : MenuScene(1920, 1080) {

    private var fontStyleEnter =
        Font(size = 34, Color.WHITE, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)

    private var fontStyleExit =
        Font(size = 30, Color.LIGHT_GRAY, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)

    val startButton = Button(
        width = 300,
        height = 50,
        text = "START GAME",
        font = fontStyleExit,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    val dialog: Dialog = Dialog(
        dialogType = DialogType.WARNING,
        title = "Warning",
        header = "File not found",
        message = ""
    )

    val loadButton = Button(
        width = 300,
        height = 50,
        text = "LOAD GAME",
        font = fontStyleExit,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    val howToPlayButton = Button(
        width = 300,
        height = 50,
        text = "HOW TO PLAY",
        font = fontStyleExit,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    val highscoreButton = Button(
        width = 300,
        height = 50,
        text = "HIGHSCORE",
        font = fontStyleExit,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    val quitButton = Button(
        width = 300,
        height = 50,
        text = "QUIT",
        font = fontStyleExit,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }


    private val menuGrid: GridPane<Button> = GridPane<Button>(
        960, 500, 1, 5, spacing = 40
    ).apply {
        setAutoColumnWidths()
        setAutoRowHeights()
        set(0, 0, startButton)
        set(0, 1, loadButton)
        set(0, 2, howToPlayButton)
        set(0, 3, highscoreButton)
        set(0, 4, quitButton)

    }

    init {
        background = ImageVisual(path = "background2.jpg")
        opacity = 1.0
        addComponents(menuGrid)
    }

    /**
     * Methode to get the link of HowToPlay.pdf
     */
    fun openURL() {
        val url = "https://docdro.id/j5coTPT"
        Desktop.getDesktop().browse(URI(url))
    }

}