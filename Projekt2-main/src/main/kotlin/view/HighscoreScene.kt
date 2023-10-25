package view

import entity.HighscoreList
import entity.Player
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import java.io.File
import kotlin.math.min

/**
 * This class shows the best players and their scores, their cards and the number of round, in which they won
 */
class HighscoreScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    var highScoreJSON = HighscoreList()


    private val highscoreLabel = Label(
        posX = 750,
        posY = 100,
        width = 500,
        height = 40,
        text = "HIGHSCORE",
        font = Font(
            50,
            color = Color.cyan,
            family = "Roboto",
            fontStyle = Font.FontStyle.OBLIQUE,
            fontWeight = Font.FontWeight.BOLD
        )
    )

    private val entireGrid = GridPane<UIComponent>(
        posX = 1000,
        posY = 580,
        rows = 15,
        columns = 5,
        spacing = 20
    )

    private val playerLabel = Label(
        width = 300,
        height = 50,
        text = "Player",
        font = Font(40, color = Color.WHITE,fontWeight = Font.FontWeight.BOLD)
    )

    private val scoreLabel = Label(
        width = 300,
        height = 50,
        text = "Score",
        font = Font(40, color = Color.WHITE,fontWeight = Font.FontWeight.BOLD)
    )
    private val cardsLabel = Label(
        width = 300,
        height = 50,
        text = "Cards",
        font = Font(40, color = Color.WHITE,fontWeight = Font.FontWeight.BOLD)
    )
    private val roundLabel = Label(
        width = 300,
        height = 50,
        text = "Round",
        font = Font(40, color = Color.WHITE,fontWeight = Font.FontWeight.BOLD)
    )

    val mainMenuButton = Button(
        width = 350,
        height = 50,
        posX = 785,
        posY = 900,
        text = "MAIN MENU",
        font = Font(30)
    )

    init {
        background = ImageVisual(path = "background.jpg")


        entireGrid[0, 0] = playerLabel
        entireGrid[1, 0] = scoreLabel
        entireGrid[2, 0] = cardsLabel
        entireGrid[3, 0] = roundLabel
        highScoreJSON.scores.sortDescending()

        try {
            highScoreJSON = rootService.gameService.loadHighScoreList()
            loadHighscore()
        }
        catch(e: Exception) {
            if(e.localizedMessage == "Detekted") {
                println("We were detekted")
            }
            println("No Highscore found")
        }
        addComponents(mainMenuButton, highscoreLabel, entireGrid)
    }

    /**
     * Loads saved highscores from a JSON file into the view and
     * displays them in this scene.
     */
    fun loadHighscore() {
        highScoreJSON.scores.subList(0, min(10,highScoreJSON.scores.size-1)).forEachIndexed{
                index, _ ->
            if (index == 9) return@forEachIndexed
            entireGrid[0, index+1] = Label(
                width = 400,
                height = 40,
                text = highScoreJSON.scores[index].name,
                font = Font(35, color = Color.WHITE)
            )
            entireGrid[1, index+1] = Label(
                width = 400,
                height = 40,
                text = highScoreJSON.scores[index].score.toString(),
                font = Font(35, color = Color.WHITE)
            )
            entireGrid[2, index+1] = Label(
                width = 400,
                height = 40,
                text = highScoreJSON.scores[index].developmentCards.toString(),
                font = Font(35, color = Color.WHITE)
            )
            entireGrid[3, index+1] = Label(
                width = 400,
                height = 40,
                text = highScoreJSON.scores[index].numRounds.toString(),
                font = Font(35, color = Color.WHITE)
            )
        }
    }

}