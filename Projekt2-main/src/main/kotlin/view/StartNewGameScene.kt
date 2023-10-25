package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * This class represents the start new game scene, where the player can enter their names, choose who to play with
 * as well as control the order of the player
 */
class StartNewGameScene : MenuScene(1920, 1080) {

    private val buttonFont = Font(size = 30, color = Color.WHITE)
    private val humanColor = ColorVisual(106, 173, 167)

    private val playernameLabel = Label(
        width = 580, height = 50, posX = 680, posY = 200,
        text = "NAME : TYPE",
        font = Font(
            size = 50,
            family = "Arial",
            color = Color.WHITE,
            fontWeight = Font.FontWeight.SEMI_BOLD,
            fontStyle = Font.FontStyle.ITALIC
        )
    )

    val startButton = Button(
        text = "START",
        width = 600,
        height = 90,
        font = Font(50, color = Color.WHITE, family = "Arial", fontStyle = Font.FontStyle.OBLIQUE),
        visual = ColorVisual(196, 198, 201, 66)
    ).apply {
        onMouseEntered = {
            font = Font(
                50, color = Color.WHITE, family = "Arial", fontStyle = Font.FontStyle.OBLIQUE,
                fontWeight = Font.FontWeight.BOLD
            )
        }
        onMouseExited = {
            font = Font(50, color = Color.WHITE, family = "Arial", fontStyle = Font.FontStyle.OBLIQUE)
        }
    }

    private val addPlayerButton = Button(
        width = 300,
        height = 65,
        text = "add player",
        font = buttonFont,
        visual = ColorVisual(196, 198, 201, 66)
    ).apply {
        onMouseClicked = {
            gridIncrease()
        }
        onMouseEntered = {
            font = Font(size = 30, color = Color.GREEN)
        }
        onMouseExited = {
            font = buttonFont
        }
    }

    private val removePlayerButton = Button(
        width = 300,
        height = 65,
        text = "remove player",
        font = buttonFont,
        visual = ColorVisual(196, 198, 201, 66)
    ).apply {
        onMouseClicked = {
            gridDecrease()
        }
        onMouseEntered = {
            font = Font(size = 30, color = Color.RED)
        }
        onMouseExited = {
            font = buttonFont
        }
    }


    val randomLabel = Label(
        text = "select for random order",
        width = 250,
        font = Font(15, color = Color.WHITE, family = "Arial"),
    )

    val randomToggle = ToggleButton(
        width = 100,
        height = 20,
        visual = Visual.EMPTY
    )

    private var name3: String = ""
    private var name4: String = ""

    private val grid =
        GridPane<TextField>(
            750,
            280,
            1,
            2,
            3,
            false,
            Visual.EMPTY
        )

    private val p1 = TextField(
        750,
        410,
        300,
        50,
        listOf("Homer", "Marge", "Bart", "Lisa", "Maggie").random()
    )
    private val p2 = TextField(
        750,
        470,
        300,
        50,
        listOf("Olaf", "Bob", "Marta", "Natsu", "Happy").random()
    )

    private val p3 = TextField().apply {
        onKeyTyped = {
            name3 = text
        }
    }
    private val p4 = TextField().apply {
        onKeyTyped = {
            name4 = text
        }
    }

    private val addAndRemoveGrid = GridPane<Button>(
        columns = 2,
        rows = 1,
        spacing = 3,
    ).apply {
        set(0, 0, addPlayerButton)
        set(1, 0, removePlayerButton)
    }

    private val startButtonGrid = GridPane<Button>(
        columns = 1,
        rows = 1,
        layoutFromCenter = true
    ).apply {
        set(0, 0, startButton)
    }

    // This Grid shows the Add and Remove Player as well as the Start Button
    private val lowerButtonsGrid = GridPane<GridPane<Button>>(
        960,
        650,
        1,
        2,
        spacing = 10,
    ).apply {
        set(0, 0, addAndRemoveGrid)
        set(0, 1, startButtonGrid)
    }

    //Grid for random option
    private val gridRandom =
        GridPane<UIComponent>(
            960,
            lowerButtonsGrid.posY + 120,
            1,
            2,
        ).apply {
            set(0, 0, randomLabel)
            set(0, 1, randomToggle)
        }

    private val player1Button = Button(
        posX = 1053,
        posY = 280,
        height = 49,
        width = 150,
        text = "Human",
        font = Font(fontWeight = Font.FontWeight.BOLD),
        visual = humanColor
    ).apply {
        onMouseClicked = {
            changeButton(text, this)
        }
    }
    private val player2Button = Button(
        posX = 1053,
        posY = 333,
        height = 49,
        width = 150,
        text = "Human",
        font = Font(fontWeight = Font.FontWeight.BOLD),
        visual = humanColor
    ).apply {
        onMouseClicked = {
            changeButton(text, this)
        }
    }
    private val player3Button = Button(
        posX = 1053,
        posY = 386,
        height = 49,
        width = 150,
        text = "Human",
        font = Font(fontWeight = Font.FontWeight.BOLD),
        visual = humanColor
    ).apply {
        onMouseClicked = {
            changeButton(text, this)
        }
    }
    private val player4Button = Button(
        posX = 1053,
        posY = 440,
        height = 49,
        width = 150,
        text = "Human",
        font = Font(fontWeight = Font.FontWeight.BOLD),
        visual = humanColor
    ).apply {
        onMouseClicked = {
            changeButton(text, this)
        }
    }
    val returnButton = Button(
        posX = 20,
        posY = 900,
        width = 200,
        height = 100,
        text = "Return",
        font = Font(30, Color.WHITE),
        visual = Visual.EMPTY
    )

    val chooseCSVSourceButton = Button(
        posX = 1550,
        posY = 900,
        width = 400,
        height = 100,
        text = "Choose CSV",
        font = Font(30, Color.WHITE),
        visual = Visual.EMPTY
    )


    init {
        background = ImageVisual("background.jpg")
        opacity = 0.4
        p1.visual = Visual.EMPTY
        grid[0, 0] = p1
        grid[0, 1] = p2
        player3Button.isVisible = false
        player4Button.isVisible = false

        addComponents(
            playernameLabel,// typeLabel,
            grid,
            lowerButtonsGrid,
            player1Button, player2Button, player3Button, player4Button, returnButton, chooseCSVSourceButton,
            gridRandom
        )
//
//        onKeyPressed ={
//            print("hello NewGame")
//        }

    }

    private fun changeButton(value: String, button: Button) {
        if (value == "Human") {
            button.text = "Bot-Easy"
            button.visual = ColorVisual(212, 67, 63)
        }
        if (value == "Bot-Easy") {
            button.text = "Bot-Medium"
        }
        if (value == "Bot-Medium") {
            button.text = "Bot-Hard"
            button.visual = ColorVisual(255, 45, 23)
        }
        if (value == "Bot-Hard") {
            button.text = "Human"
            button.visual = ColorVisual(106, 173, 167)
        }
    }

    private fun gridIncrease() {
        if (grid.rows < 3) {
            val p3 = TextField(
                750,
                100,
                300,
                50,
                listOf("Hussein", "Ali", "Paolo", "Brigitte", "Kevin").random()
            ).apply {
                onKeyTyped = {
                    name3 = text
                }
            }
            name3 = p3.text
            grid.addRows(2)
            grid[0, 2] = p3
            player3Button.isVisible = true
            return
        }
        if (grid.rows < 4) {
            val p4 = TextField(
                750,
                680,
                300,
                50,
                listOf("Sigma", "Gabriel", "Angela", "Peter", "Luka").random()
            ).apply {
                onKeyTyped = {
                    name4 = text
                }
            }
            name4 = p4.text
            grid.addRows(3)
            grid[0, 3] = p4
            player4Button.isVisible = true
        }

    }

    private fun gridDecrease() {
        if (grid.rows == 4) {
            removeComponents(p4)
            grid.removeRow(3)
            player4Button.isVisible = false
            return
        }
        if (grid.rows == 3) {
            removeComponents(p3)
            player3Button.isVisible = false
            grid.removeRow(2)
        }
    }

    /**
     * function to create player list, which contains the player's name along with their type
     */
    fun createPlayerList(): List<Player> {
        val listPlayer = mutableListOf<Player>()
        val type = arrayOf(player1Button.text, player2Button.text, player3Button.text, player4Button.text)
        val names = arrayOf(p1.text, p2.text, name3, name4)
        val nameSet = mutableSetOf<String>()
        for (i in 0 until grid.rows) {
            require(names[i].isNotEmpty()) { "Leere Namen sind nicht erlaubt" }
            nameSet.add(names[i])
            listPlayer.add(
                i, Player(
                    names[i],
                    getPlayerType(type[i])
                )
            )
        }
        require(nameSet.size == grid.rows) { "Doppelte Namen sind nicht erlaubt" }
        if (randomToggle.selectedProperty.value) {
            listPlayer.shuffle()
        }
        return listPlayer
    }

    private fun getPlayerType(p: String): PlayerType {
        val playerName = p.lowercase()
        PlayerType.values().forEach {
            if (playerName.contains(it.name.lowercase())) return it
        }
        return PlayerType.HUMAN
    }

}