package view


import entity.PlayerType
import service.RootService
import service.ai.HardAI
import service.ai.MediumAI
import service.ai.SimpleAI
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import java.io.File


/**
 * This class represents the application, which controls the changing of scenes
 */
class SplendorApp : BoardGameApplication("Splendor") {

    private val rootService = RootService()
    private val mainMenueScene = MainMenuScene()
    val newGameScene = StartNewGameScene()
    private var selectedFiles: List<File> = listOf()

    val pauseGameScene = PauseGameScene()

     val highscoreScene = HighscoreScene(rootService).apply { refreshAfterGameFinished(0) }



    init {
        this.showMenuScene(mainMenueScene)


        mainMenueScene.apply {
            startButton.onMouseClicked = {
                hideMenuScene()
                this@SplendorApp.showMenuScene(newGameScene, 750)
            }
            howToPlayButton.onMouseClicked = {
                mainMenueScene.openURL()
            }

            loadButton.onMouseClicked = {
                val fileName = "save1.sav"
                val saveFile = File(fileName)
                if (!saveFile.exists()) {
                    showDialog(dialog)
                } else {
                    rootService.playerActionService.loadGame(1)
                    val game = SplendorGameScene(rootService, this@SplendorApp)
                    rootService.addRefreshable(game)
                    hideMenuScene()
                    showGameScene(game)
                }

            }

            quitButton.onMouseClicked = {
                exit()
            }
            highscoreButton.onMouseClicked = {
                this@SplendorApp.showMenuScene(highscoreScene, 750)
            }
        }

        newGameScene.apply {
            returnButton.onMouseClicked = {
                hideMenuScene()
                this@SplendorApp.showMenuScene(mainMenueScene, 750)
            }
            chooseCSVSourceButton.onMouseClicked = {
                val fileDialog = FileDialog(FileDialogMode.OPEN_MULTIPLE_FILES, "Select two CSV Files")
                val fileList = this@SplendorApp.showFileDialog(fileDialog)
                check(!fileList.isEmpty) { "Keine Datei ausgewählt" }
                check(fileList.get().size == 2) { "Falsche Anzahl von Dateien ausgewählt" }
                check(fileList.get()[0].extension == "csv" && fileList.get()[1].extension == "csv") { "Falscher Dateityp" }
                selectedFiles = fileList.get()
            }
            startButton.onMouseClicked = {
                rootService.gameService.startNewGame(newGameScene.createPlayerList(), selectedFiles)
                val game = SplendorGameScene(rootService, this@SplendorApp)
                rootService.addRefreshable(game)
                hideMenuScene()
                showGameScene(game)
                val currentPlayer = rootService.gameService.getCurrentPlayer()
                if (currentPlayer.playerType != PlayerType.HUMAN) {
                    val ai = when (currentPlayer.playerType) {
                        PlayerType.EASY -> SimpleAI()
                        PlayerType.MEDIUM -> MediumAI()
                        PlayerType.HARD -> HardAI()
                        else -> SimpleAI()
                    }
                    val move = ai.determineBestMove(rootService.currentGame.getCurrentGame(), 5000)
                    move.perform(rootService)
                }
                pauseGameScene.apply {
                    quitButton.onMouseClicked = {
                        exit()
                    }
                    saveGameButton.onMouseClicked = {
                        rootService.playerActionService.saveGame(1)
                        hideMenuScene(500)
                    }

                    resumeButton.onMouseClicked = {
                        hideMenuScene(500)
                    }
                }
            }
        }

        highscoreScene.apply {
            mainMenuButton.onMouseClicked = {
                hideMenuScene()
                this@SplendorApp.showMenuScene(mainMenueScene, 750)
            }
        }

    }
}


