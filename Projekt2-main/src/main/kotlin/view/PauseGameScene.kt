package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * This class represents the pause game scene, where the player can resume, quit or save game
 */
class PauseGameScene:MenuScene(900, 800) {
    private val fontEntered =
        Font(size = 34, Color.WHITE, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)
    private val fontExited =
        Font(size = 30, Color.LIGHT_GRAY, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)

    private val entireGrid = GridPane<UIComponent>(
        posX = 450,
        posY = 400,
        rows = 3,
        columns = 1,
        spacing = 30
    )
    val pauseLabel = Label(
        posX = 230,
        posY = 200,
        width = 450,
        height = 50,
        text = "PAUSE",
        font = Font(40, Color.WHITE,family = "ComicSans",Font.FontWeight.BOLD,Font.FontStyle.OBLIQUE)
    )
    
    val resumeButton = Button(
        width = 300,
        height = 50,
        text = "Resume",
        font = fontExited,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered ={
            font = fontEntered
        }
        onMouseExited = {
            font = fontExited
        }
    }
    val saveGameButton = Button(
        width = 300,
        height = 50,
        text = "Save game",
        font = Font(30,Color.WHITE),
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered ={
            font = fontEntered
        }
        onMouseExited = {
            font = fontExited
        }
    }

    val quitButton = Button(
        width = 300,
        height = 50,
        text = "Quit",
        font = Font(30,Color.WHITE),
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered ={
            font = fontEntered
        }
        onMouseExited = {
            font = fontExited
        }
    }
    
    init{
        background = ImageVisual("background.jpg")
        opacity = 0.67
        entireGrid[0,0] = resumeButton
        entireGrid[0,1] = saveGameButton
        entireGrid[0,2] = quitButton
        
        addComponents(entireGrid, pauseLabel)
    }
}