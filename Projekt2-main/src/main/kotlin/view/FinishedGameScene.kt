package view

import entity.GameState
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene

import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * This class represents the finished game scene, where the winner and player's points will be shown
 */
class FinishedGameScene(private val rootService: RootService) : MenuScene(1920, 1080){

    private val gameService = rootService.gameService

    /**
     * function to get the list score when there's more than 2 players
     */


    val winnerLabel = Label(
        posX = 700,
        posY = 200,
        width = 700,
        height = 40,
        text = "",
        font = Font(45, color = Color.WHITE)
    )
    private val entireGrid = GridPane<Label>(
        posX = 900,
        posY = 500,
        rows = 6,
        columns = 1,
        spacing = 30
    )
    val quitButton = Button(
        posX = 1100,
        posY = 900,
        width = 350,
        height = 50,
        text = "QUIT",
        font = Font(30)
    )

    val highscoreButton = Button(
        posX = 600,
        posY = 900,
        width = 350,
        height = 50,
        text = "HIGHSCORE",
        font = Font(30)
    )

    init{
        background = ImageVisual(path = "background.jpg")
        if(getGame().passingCounter == getGame().players.size){
            entireGrid[0,0] = Label(
                posX = 800,
                posY = 200,
                width = 400,
                height = 40,
                text = "The game is tied",
                font = Font(45, color = Color.WHITE)
            )
        }
        else {
            val playerList = getGame().players.toMutableList()
            val winner = playerList[gameService.getBestPlayerIndex()]

            winnerLabel.text = "WINNER: " + winner.name + " with ${gameService.calculatePoints(winner)} Points!"
            playerList.remove(getGame().players[gameService.getBestPlayerIndex()])

            playerList.forEachIndexed{index, player ->
                val label = Label(
                    width =400,
                    height =40,
                    text = "${playerList[index].name}: " + "${gameService.calculatePoints(playerList[index])}",
                    font = Font(30, color = Color.WHITE)
                )
                entireGrid[0,index] = label
            }
        }

    addComponents(entireGrid,winnerLabel, quitButton, highscoreButton)
    }
    private fun getGame(): GameState {
        return rootService.currentGame.getCurrentGame()
    }

}