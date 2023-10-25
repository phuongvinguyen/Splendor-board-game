package view

import entity.*
import service.RootService
import service.SourceStack
import service.TurnType
import service.Yielder
import service.ai.HardAI
import service.ai.MediumAI
import service.ai.SimpleAI
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color


const val PLAYER1_TOKEN_POSX = 815
const val PLAYER1_TOKEN_POSY = 900
const val PLAYER2_TOKEN_POSX = 1800
const val PLAYER2_TOKEN_POSY = 800
const val PLAYER3_TOKEN_POSX = 1250
const val PLAYER3_TOKEN_POSY = 50
const val PLAYER4_TOKEN_POSX = 100
const val PLAYER4_TOKEN_POSY = 300

const val GAME_TOKEN_POSX = 1175
const val GAME_TOKEN_POSY = 210

/**
 * This class describes the main game scene, where the player takes their turn, show hint or pause game
 */
class SplendorGameScene(private val rootService: RootService, private val splendorApp: SplendorApp) :
    BoardGameScene(1920, 1080), Refreshable {

    private val devLoad = DevelopmentCardImageLoader()
    private val nobLoad = NobleCardImageLoader()
    private val action = rootService.playerActionService
    private var targetedCard: DevelopmentCard = rootService.currentGame.getCurrentGame().drawCards.flatten()[1]
    private var targetedTokenList = mutableListOf<Token>()
    private val buyCardTokenMap = mutableMapOf<Token, Int>()
    private var targetedToken = Token.EMERALD
    private var targetedNoble = rootService.currentGame.getCurrentGame().nobleTiles[1]
    private var _currentYielder: Yielder<Unit>? = null
    private var currentYielder
        get() = _currentYielder
        set(value) {
            println("Yielder wird aufgerufen")
            _currentYielder = value
            currentYielder?.continueWith(null)
        }
    private var nobleTargeted = false
    private var tookToken = false
    private var allowToClickNoble = true
    private var cardViewStatus = false
    private var tokenViewStatus = false
    private var allowToClickCard = true
    private var allowToClickToken = true
    private var allowToClickStack = true
    private var tokenCount = 0
    private var nobleCount = 0

    private var playerToPosXFull =
        listOf(PLAYER1_TOKEN_POSX, PLAYER2_TOKEN_POSX, PLAYER3_TOKEN_POSX, PLAYER4_TOKEN_POSX)
    private var playerToPosYFull =
        listOf(PLAYER1_TOKEN_POSY, PLAYER2_TOKEN_POSY, PLAYER3_TOKEN_POSY, PLAYER4_TOKEN_POSY)
    private var playerTokenOrientationFull =
        listOf(
            Pair(Orientation.HORIZONTAL, false),
            Pair(Orientation.VERTICAL, false),
            Pair(Orientation.HORIZONTAL, true),
            Pair(Orientation.VERTICAL, true)
        )

    private var playerToPosX = listOf<Int>()
    private var playerToPosY = listOf<Int>()
    private var playerTokenOrientation = listOf<Pair<Orientation, Boolean>>()

    private val playerToTokenToLinearLayout = mutableMapOf<Int, MutableMap<Token, LinearLayout<CardView>>>()

    private val playerToLayoutContainer = mutableMapOf<Int, LinearLayoutContainer<CardView>>()

    private val gameTokenContainer: LinearLayoutContainer<CardView>

    private val gameTokenToLinearLayout = mutableMapOf<Token, LinearLayout<CardView>>()

    private var pauseCounter = 0

    private var p1SapphireBonus = ""
    private var p1RubyBonus = ""
    private var p1OnyxBonus = ""
    private var p1EmeraldBonus = ""
    private var p1DiamondBonus = ""

    private var p2SapphireBonus = ""
    private var p2RubyBonus = ""
    private var p2OnyxBonus = ""
    private var p2EmeraldBonus = ""
    private var p2DiamondBonus = ""

    private var p3SapphireBonus = ""
    private var p3RubyBonus = ""
    private var p3OnyxBonus = ""
    private var p3EmeraldBonus = ""
    private var p3DiamondBonus = ""

    private var p4SapphireBonus = ""
    private var p4RubyBonus = ""
    private var p4OnyxBonus = ""
    private var p4EmeraldBonus = ""
    private var p4DiamondBonus = ""


    private val noNextRoundText =
        Label(10, 850, 250, 50, "There is no next round", Font(24, Color.WHITE)).apply { isVisible = false }
    private val noPreviousRoundText =
        Label(10, 850, 300, 50, "There is no prevoius round", Font(24, Color.WHITE)).apply { isVisible = false }
    private val tokenDrawNotAllowed = Label(
        1150,
        750,
        300,
        100,
        "Desired Token draw not allowed, try again!",
        Font(24, Color.RED),
        isWrapText = true
    ).apply { isVisible = false }

    private val tier3Stack =
        CardStack<CardView>(
            450,
            174,
            130,
            200,
            Alignment.CENTER,
            ImageVisual(
                DevelopmentCardImageLoader().backImageForTier(3)
            ).apply { transparency = 0.3 })

    private val tier2Stack =
        CardStack<CardView>(
            450,
            405,
            130,
            200,
            Alignment.CENTER,
            ImageVisual(
                DevelopmentCardImageLoader().backImageForTier(2)
            ).apply { transparency = 0.3 })

    private val tier1Stack =
        CardStack<CardView>(
            450,
            636,
            130,
            200,
            Alignment.CENTER,
            ImageVisual(
                DevelopmentCardImageLoader().backImageForTier(1)
            ).apply { transparency = 0.3 })

    private val tier3Cards = LinearLayout<CardView>(600, 174, 1000, 160, spacing = 8)

    private val tier2Cards = LinearLayout<CardView>(600, 405, 1000, 160, spacing = 8)

    private val tier1Cards = LinearLayout<CardView>(600, 636, 1000, 160, spacing = 8)


    // Styles for Mouse-Effects
    private var fontStyleEnter =
        Font(size = 13, Color.WHITE, fontStyle = Font.FontStyle.NORMAL, fontWeight = Font.FontWeight.BOLD)

    private var fontStyleExit =
        Font(size = 13, Color.WHITE, fontWeight = Font.FontWeight.NORMAL)

    private val moveHistoryGrid = GridPane<Label>(
        1700,
        50,
        1,
        5,
        spacing = 2.0,
        layoutFromCenter = false,
    )

    private val moveHistoryLabel = Label(
        moveHistoryGrid.posX,
        moveHistoryGrid.posY - 25,
        180,
        21,
        "MOVE HISTORY",
        Font(15, Color.black, fontWeight = Font.FontWeight.SEMI_BOLD, fontStyle = Font.FontStyle.OBLIQUE),
        Alignment.CENTER,
        visual = ColorVisual(203, 210, 217, 220),
    )


    private val redoButton = Button(
        moveHistoryGrid.posX,
        moveHistoryGrid.posY - 25,
        30,
        30,
        "⏭",
        Font(15, Color.black, fontWeight = Font.FontWeight.SEMI_BOLD, fontStyle = Font.FontStyle.OBLIQUE),
        Alignment.CENTER,
        visual = ColorVisual(203, 210, 217, 220),
    ).apply {
        onMouseClicked = {
            try {
                action.redo()
            } catch (_: IllegalStateException) {
                noNextRoundText.isVisible = true
                playAnimation(DelayAnimation(2000).apply { onFinished = { noNextRoundText.isVisible = false } })
            }
        }
    }

    private val takeTokenForBuy = Button(
        0,
        0,
        100,
        100,
        text = "Select",
        font = fontStyleExit,
        visual = ColorVisual(106, 173, 167)
    ).apply {
        isVisible = false
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val undoButton = Button(
        moveHistoryGrid.posX,
        moveHistoryGrid.posY + 100,
        30,
        30,
        "⏮",
        Font(15, Color.black, fontWeight = Font.FontWeight.SEMI_BOLD, fontStyle = Font.FontStyle.OBLIQUE),
        Alignment.CENTER,
        visual = ColorVisual(203, 210, 217, 220),
    ).apply {
        onMouseClicked = {
            try {
                action.undo()
            } catch (_: IllegalStateException) {
                noPreviousRoundText.isVisible = true
                playAnimation(DelayAnimation(2000).apply { onFinished = { noPreviousRoundText.isVisible = false } })
            }
        }
    }

    private val tokenViewLayout =
        LinearLayout<TokenView>(
            1150,
            190,
            150,
            1000,
            orientation = Orientation.VERTICAL,
            spacing = 5
        )
    private val nobleSelectButton = Button(
        width = 180,
        height = 25,
        text = "Select",
        font = fontStyleExit,
        visual = ColorVisual(106, 173, 167)
    ).apply {
        isVisible = false
        onMouseClicked = {
            if (nobleTargeted) {
                currentYielder?.continueWith(targetedNoble)
            }
        }
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val reserveButtonForCards =
        Button(
            600,
            150,
            text = "Reserve",
            width = 130,
            height = 25,
            visual = ColorVisual(106, 173, 167),
            font = fontStyleExit

        ).apply {
            isVisible = false
            onMouseClicked = {
                action.reserveCard(targetedCard, SourceStack.open(targetedCard.tier))
                currentYielder = rootService.gameService.startNextRound()
            }
            onMouseEntered = {
                font = fontStyleEnter
            }
            onMouseExited = {
                font = fontStyleExit
            }
        }


    private val buyButtonForCards =
        Button(
            600,
            100,
            text = "Buy",
            width = 130,
            height = 25,
            visual = ColorVisual(106, 173, 167),
            font = fontStyleExit
        ).apply {
            isVisible = false
            onMouseClicked = {
                confirmBuyButton.isVisible = true
                makeTokenSelectionAvailable()
            }
            onMouseEntered = {
                font = fontStyleEnter
            }
            onMouseExited = {
                font = fontStyleExit
            }
        }

    private val takeOneButtonForTokens = Button(
        1200,
        150,
        text = "Take One",
        width = 130,
        visual = ColorVisual(106, 173, 167),
        font = fontStyleExit
    ).apply {
        isVisible = false
        onMouseClicked = {
            println("targeted Token: $targetedToken")
            takeOneToken()
            println(targetedTokenList.toString())
        }
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val takeTwoButtonForTokens =
        Button(
            1200,
            200, text = "Take Two",
            width = 130,
            visual = ColorVisual(106, 173, 167),
            font = fontStyleExit
        ).apply {
            isVisible = false
            onMouseClicked = {
                takeTwoTokens()
            }
            onMouseEntered = {
                font = fontStyleEnter
            }
            onMouseExited = {
                font = fontStyleExit
            }

        }

    private val reserveButtonForStack =
        Button(
            600,
            150,
            text = "Reserve",
            width = 130,
            height = 25,
            visual = ColorVisual(106, 173, 167),
            font = fontStyleExit
        ).apply {
            isVisible = false
            onMouseClicked = {
                action.reserveCard(targetedCard, SourceStack.draw(targetedCard.tier))
                currentYielder = rootService.gameService.startNextRound()

            }
            onMouseEntered = {
                font = fontStyleEnter
            }
            onMouseExited = {
                font = fontStyleExit
            }
        }

    private val confirmBuyButton = Button(
        1000,
        865,
        130,
        30,
        text = "Confirm Buy",
        font = fontStyleExit,
        visual = ColorVisual(106, 173, 167)
    ).apply {
        isVisible = false
        onMouseClicked = {
            takeTokenForBuy.isVisible = false
            buyCard()
            isVisible = false
        }
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val selectDiscardTokenButton = Button(
        0,
        0,
        130,
        30,
        "Select Token",
        font = fontStyleExit,
        visual = ColorVisual(106, 173, 167)
    ).apply {
        isVisible = false
        onMouseClicked = {
            isVisible = true
            confirmDiscardTokenButton.isVisible = true
        }
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val confirmDiscardTokenButton = Button(
        1000,
        865,
        130,
        30,
        text = "Confirm Discard",
        visual = ColorVisual(106, 173, 167),
        font = fontStyleExit
    ).apply {
        isVisible = false
        onMouseClicked = {
            discardTokens()
            isVisible = false
        }
        onMouseEntered = {
            font = fontStyleEnter
        }
        onMouseExited = {
            font = fontStyleExit
        }
    }

    private val filler = Label(
        text = " ",
        height = 20,
        width = 20
    )

    // Labels of gems
    private val sapphirePic = Label(
        height = 50, width = 50,
        visual = ImageVisual("sapphire.png")
    )
    private val onyxPic = Label(
        height = 50, width = 50,
        visual = ImageVisual("onyx.png")
    )
    private val diamondPic = Label(
        height = 50, width = 50,
        visual = ImageVisual("diamond.png")
    )
    private val rubyPic = Label(
        height = 50, width = 50,
        visual = ImageVisual("ruby.png")
    )
    private val emeraldPic = Label(
        height = 50, width = 50,
        visual = ImageVisual("emerald.png")
    )

    // Bonus Label for Player 1
    private val p1BonusRubyLabel = Label(
        text = p1RubyBonus,
        font = Font(23, Color.BLACK)
    )
    private val p1BonusEmeraldLabel = Label(
        text = p1EmeraldBonus,
        font = Font(23, Color.BLACK)
    )
    private val p1BonusSapphireLabel = Label(
        text = p1SapphireBonus,
        font = Font(23, Color.BLACK)
    )
    private val p1BonusOnyxLabel = Label(
        text = p1OnyxBonus,
        font = Font(23, Color.BLACK)
    )
    private val p1BonusDiamondLabel = Label(
        text = p1DiamondBonus,
        font = Font(23, Color.BLACK),
        height = 50
    )
    private val p1PlayerLabel = Label(
        text = "",
        height = 100,
        isWrapText = true,
        font = Font(24, Color.BLACK, fontWeight = Font.FontWeight.BOLD)
    )

    // Bonus Label for Player 2
    private val p2BonusRubyLabel = Label(
        text = p2RubyBonus,
        font = Font(23, Color.BLACK)
    )
    private val p2BonusEmeraldLabel = Label(
        text = p2EmeraldBonus,
        font = Font(23, Color.BLACK)
    )
    private val p2BonusSapphireLabel = Label(
        text = p2SapphireBonus,
        font = Font(23, Color.BLACK)
    )
    private val p2BonusOnyxLabel = Label(
        text = p2OnyxBonus,
        font = Font(23, Color.BLACK)
    )
    private val p2BonusDiamondLabel = Label(
        text = p2DiamondBonus,
        font = Font(23, Color.BLACK),
        height = 50
    )
    private val p2PlayerLabel = Label(
        text = "",
        height = 100,
        isWrapText = true,
        font = Font(24, Color.BLACK, fontWeight = Font.FontWeight.BOLD)
    )

    // Bonus Label for Player 1
    private val p3BonusRubyLabel = Label(
        text = p3RubyBonus,
        font = Font(23, Color.BLACK)
    )
    private val p3BonusEmeraldLabel = Label(
        text = p3EmeraldBonus,
        font = Font(23, Color.BLACK)
    )
    private val p3BonusSapphireLabel = Label(
        text = p3SapphireBonus,
        font = Font(23, Color.BLACK)
    )
    private val p3BonusOnyxLabel = Label(
        text = p3OnyxBonus,
        font = Font(23, Color.BLACK)
    )
    private val p3BonusDiamondLabel = Label(
        text = p3DiamondBonus,
        font = Font(23, Color.BLACK),
        height = 50
    )
    private val p3PlayerLabel = Label(
        text = "",
        height = 100,
        isWrapText = true,
        font = Font(24, Color.BLACK, fontWeight = Font.FontWeight.BOLD)
    )

    // Bonus Label for Player 1
    private val p4BonusRubyLabel = Label(
        text = p4RubyBonus,
        font = Font(23, Color.BLACK)
    )
    private val p4BonusEmeraldLabel = Label(
        text = p4EmeraldBonus,
        font = Font(23, Color.BLACK)
    )
    private val p4BonusSapphireLabel = Label(
        text = p4SapphireBonus,
        font = Font(23, Color.BLACK)
    )
    private val p4BonusOnyxLabel = Label(
        text = p4OnyxBonus,
        font = Font(23, Color.BLACK)
    )
    private val p4BonusDiamondLabel = Label(
        text = p4DiamondBonus,
        font = Font(23, Color.BLACK),
        height = 50
    )
    private val p4PlayerLabel = Label(
        text = "",
        height = 100,
        isWrapText = true,
        font = Font(24, Color.BLACK, fontWeight = Font.FontWeight.BOLD)
    )


    private val currentPlayerBonusGrid = GridPane<Label>(
        columns = 5,
        rows = 8,
        layoutFromCenter = true,
    ).apply {

        set(0, 0, filler)
        set(2, 2, rubyPic)
        set(2, 3, emeraldPic)
        set(2, 4, sapphirePic)
        set(2, 5, onyxPic)
        set(2, 6, diamondPic)

        set(2, 1, p1PlayerLabel)
        set(3, 2, p1BonusRubyLabel)
        set(3, 3, p1BonusEmeraldLabel)
        set(3, 4, p1BonusSapphireLabel)
        set(3, 5, p1BonusOnyxLabel)
        set(3, 6, p1BonusDiamondLabel)

        set(4, 7, filler)
    }

    private val p2PlayerBonusGrid = GridPane<Label>(
        columns = 5,
        rows = 8,
        layoutFromCenter = true,
    ).apply {

        set(0, 0, filler)

        set(2, 1, p2PlayerLabel)
        set(3, 2, p2BonusRubyLabel)
        set(3, 3, p2BonusEmeraldLabel)
        set(3, 4, p2BonusSapphireLabel)
        set(3, 5, p2BonusOnyxLabel)
        set(3, 6, p2BonusDiamondLabel)

        set(4, 7, filler)
    }

    private val p3PlayerBonusGrid = GridPane<Label>(
        columns = 5,
        rows = 8,
        layoutFromCenter = true,
    ).apply {

        set(0, 0, filler)

        set(2, 1, p3PlayerLabel)
        set(3, 2, p3BonusRubyLabel)
        set(3, 3, p3BonusEmeraldLabel)
        set(3, 4, p3BonusSapphireLabel)
        set(3, 5, p3BonusOnyxLabel)
        set(3, 6, p3BonusDiamondLabel)

        set(4, 7, filler)
    }

    private val p4PlayerBonusGrid = GridPane<Label>(
        columns = 5,
        rows = 8,
        layoutFromCenter = true,
    ).apply {

        set(0, 0, filler)
        set(2, 1, p4PlayerLabel)
        set(3, 2, p4BonusRubyLabel)
        set(3, 3, p4BonusEmeraldLabel)
        set(3, 4, p4BonusSapphireLabel)
        set(3, 5, p4BonusOnyxLabel)
        set(3, 6, p4BonusDiamondLabel)
        set(4, 7, filler)
    }

    // Final Grids
    //Bottom Player
    private val currentPlayerBonusGridPlus = GridPane<GridPane<Label>>(
        1370,
        875,
        columns = 3,
        rows = 3,
        visual = ImageVisual("paper.png"),
        spacing = 15
    ).apply {
        set(1, 1, currentPlayerBonusGrid)
    }.apply {
        scale(0.70)

    }

    //Left Player
    private val p2PlayerBonusGridPlus = GridPane<GridPane<Label>>(
        180,
        330,
        columns = 3,
        rows = 3,
        spacing = 15
    ).apply {
        set(1, 1, p2PlayerBonusGrid)
    }.apply { scale(0.4) }

    //Top Player
    private val p3PlayerBonusGridPlus = GridPane<GridPane<Label>>(
        1315,
        -30,
        columns = 3,
        rows = 3,
        spacing = 15
    ).apply {
        set(1, 1, p3PlayerBonusGrid)
    }.apply { scale(0.4) }

    //Right Player
    private val p4PlayerBonusGridPlus = GridPane<GridPane<Label>>(
        1600,
        330,
        columns = 3,
        rows = 3,
        spacing = 15
    ).apply {
        set(1, 1, p4PlayerBonusGrid)
    }.apply {
        scale(0.4)
    }

    private val hintButton = Button(
        25,
        900,
        325,
        80,
        text = "What would Chuck Norris do?",
        font = Font(20, Color.DARK_GRAY, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(203, 210, 217, 220),
        alignment = Alignment.CENTER,
    ).apply {
        onMouseClicked = {
            isVisible = false
            hintLabel.isVisible = true
            hintLabel.text = action.getHint()
        }
        onMouseEntered = {
            font = Font(20, Color.BLACK, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)
        }
        onMouseExited = {
            font = Font(20, Color.DARK_GRAY, fontStyle = Font.FontStyle.OBLIQUE, fontWeight = Font.FontWeight.BOLD)
        }
    }

    private val hintLabel = Label(
        25, 900,
        325,
        80,
        text = "",
        isWrapText = true,
        font = Font(size = 12, Color.BLACK, fontStyle = Font.FontStyle.NORMAL),
        visual = ColorVisual(203, 210, 217, 125),
        alignment = Alignment.CENTER
    ).apply { isVisible = false }

    private val undoRedoGrid = GridPane<Button>(
        hintLabel.posX,
        hintLabel.posY + 100,
        columns = 2,
        rows = 1,
        spacing = 10.0,
        layoutFromCenter = false,

        ).apply {
        set(0, 0, undoButton)
        set(1, 0, redoButton)
    }

    private val botSpeedToggle = ToggleButton(
        undoRedoGrid.posX + 120,
        undoRedoGrid.posY - 15,
    )

    private val botSpeedSlowLabel = Label(
        botSpeedToggle.posX - 35,
        botSpeedToggle.posY + 14,
        text = "SLOW",
        width = 50,
        font = Font(size = 12, Color.WHITE, fontStyle = Font.FontStyle.NORMAL),
    )

    private val botSpeedFastLabel = Label(
        botSpeedToggle.posX + 55,
        botSpeedToggle.posY + 14,
        text = "FAST",
        width = 50,
        font = Font(size = 12, Color.WHITE, fontStyle = Font.FontStyle.ITALIC)
    )

    private val nobleLayout1 =
        LinearLayout<CardView>(
            1250,
            200,
            130,
            800,
            spacing = 8,
            orientation = Orientation.VERTICAL
        )
    private val nobleLayout2 =
        LinearLayout<CardView>(
            1410,
            300,
            130,
            800,
            spacing = 8,
            orientation = Orientation.VERTICAL
        )
    private val reservedCardsCurrentPlayer =
        LinearLayout<CardView>(
            400,
            875,
            3 * (tier2Cards.width + 5),
            200,
            spacing = 8,
        )
    private val reserveCardPlayerLeft =
        LinearLayout<CardView>(
            50,
            175,
            130,
            600,
            -60,
            Visual.EMPTY
        )
    private val reserveCardPlayerOnTop =
        LinearLayout<CardView>(
            600,
            20,
            420,
            130,
            -20,
            Visual.EMPTY
        )
    private val reserveCardPlayerRight =
        LinearLayout<CardView>(
            1620,
            550,
            130,
            600,
            -50,
            Visual.EMPTY
        )


    private val nobleSelection = LinearLayout<CardView>(
        1400,
        800,
        800,
        180,
        spacing = 5,
        Visual.EMPTY
    ).apply { isVisible = false }

    init {
        background = ImageVisual("wood.jpg")

        addComponents(
            nobleLayout1, nobleLayout2,
            tier1Stack, tier2Stack, tier3Stack,
            tier1Cards, tier2Cards, tier3Cards,
            tokenViewLayout, noPreviousRoundText,
            noNextRoundText, tokenDrawNotAllowed,
            hintButton, nobleSelectButton,
            reserveButtonForCards, buyButtonForCards,
            takeOneButtonForTokens, takeTwoButtonForTokens,
            reserveButtonForStack, moveHistoryLabel,
            moveHistoryGrid, nobleSelection,
            reservedCardsCurrentPlayer,
            reserveCardPlayerLeft,
            reserveCardPlayerOnTop,
            reserveCardPlayerRight,
            hintLabel, undoRedoGrid,
            currentPlayerBonusGridPlus, p2PlayerBonusGridPlus, p3PlayerBonusGridPlus, p4PlayerBonusGridPlus,
            botSpeedToggle, botSpeedSlowLabel, botSpeedFastLabel
        )
        onKeyPressed = {
            if (it.keyCode.string == "Esc" && pauseCounter == 0) {
                pauseCounter++
                splendorApp.showMenuScene(splendorApp.pauseGameScene, 300)
            } else if (it.keyCode.string == "Esc" && pauseCounter != 0) {
                pauseCounter = 0
                splendorApp.hideMenuScene(300)
            }
        }
        createAllCards()

        val game = rootService.currentGame.getCurrentGame()

        when (game.players.size) {
            2 -> playerToPosX = playerToPosXFull.toMutableList().apply { removeAt(3); removeAt(1) }
                .also {
                    playerToPosY = playerToPosYFull.toMutableList().apply { removeAt(3); removeAt(1) }
                }.also {
                    playerTokenOrientation =
                        playerTokenOrientationFull.toMutableList().apply { removeAt(3); removeAt(1) }
                }
            3 -> playerToPosX = playerToPosXFull.toMutableList().apply { removeAt(2) }
                .also { playerToPosY = playerToPosYFull.toMutableList().apply { removeAt(2) } }.also {
                    playerTokenOrientation =
                        playerTokenOrientationFull.toMutableList().apply { removeAt(2) }
                }
            4 -> playerToPosX =
                playerToPosXFull.toMutableList().also { playerToPosY = playerToPosYFull.toMutableList() }
                    .also { playerTokenOrientation = playerTokenOrientationFull.toMutableList() }
            else -> throw IllegalStateException()
        }

        game.players.forEachIndexed { index, _ ->
            val tokenLinearLayoutContainer = LinearLayoutContainer<CardView>(
                posX = playerToPosX[index],
                posY = playerToPosY[index],
                createLinearLayouts = 6,
                createLinearLayoutsHeight = 80,
                createLinearLayoutsWidth = 80
            )
            val tokenToLinearLayoutMap = mutableMapOf<Token, LinearLayout<CardView>>()
            Token.values().forEachIndexed { ind, token ->
                tokenToLinearLayoutMap[token] = tokenLinearLayoutContainer[ind]
            }
            playerToTokenToLinearLayout[index] = tokenToLinearLayoutMap
            playerToLayoutContainer[index] = tokenLinearLayoutContainer


        }

        playerToLayoutContainer.values.forEach { addComponents(*it.toTypedArray()) }
        gameTokenContainer = LinearLayoutContainer(
            GAME_TOKEN_POSX,
            GAME_TOKEN_POSY,
            orientation = Orientation.VERTICAL,
            backwards = true,
            createLinearLayouts = 6,
            createLinearLayoutsWidth = 1,
            createLinearLayoutsHeight = 80,
            spacing = 10
        )
        gameTokenContainer.forEachIndexed { index, linearLayout ->
            gameTokenToLinearLayout[Token.values()[index]] = linearLayout
            addComponents(linearLayout)
        }
        val tokenList = mutableListOf<Token>()
        game.tokens.forEach { (k, v) ->
            repeat(v) { tokenList.add(k) }
        }
        createTokenStackView(tokenList, gameTokenToLinearLayout)
        refreshPlayerTokenRotationAndPosition()

        gameTokenContainer.forEachIndexed { index, token ->
            token.onMouseClicked = {
                createButtonsForTokens(Token.values()[index], index)
            }
        }
        addComponents(takeTokenForBuy, confirmBuyButton, confirmDiscardTokenButton, selectDiscardTokenButton)
    }

    /**
     * create View Methods
     */

    private fun createMoveHistory(list: MutableList<String>) {

        list.takeLast(4).forEachIndexed { index, element ->
            val label = Label(
                width = 180,
                height = 75,
                text = element,
                isWrapText = true,
                font = Font(size = 11, color = Color.BLACK, fontWeight = Font.FontWeight.LIGHT),
                visual = ColorVisual(203, 210, 217, 165),
            )
            moveHistoryGrid[0, index] = label
        }
    }


    private fun createViewForReserved(
        stack: List<DevelopmentCard>,
        linearLayout: LinearLayout<CardView>,
        rotation: Int
    ) {
        stack.forEach { card ->
            val cardView = CardView(
                0,
                0,
                80,
                120,
                front = ImageVisual(devLoad.createCompleteCardImage(card)),
                back = ImageVisual(devLoad.backImageForTier(card.tier))
            )
            cardView.rotate(rotation)
            cardView.showFront()
            linearLayout.add(cardView)
        }
    }

    private fun createPlayerView(playersize: Int) {
        val game = rootService.currentGame.getCurrentGame()

        // Player at the bottom
        p1BonusSapphireLabel.text = "${
            rootService.gameService.getCurrentPlayer()
                .cards.count { it.bonus == Token.SAPPHIRE }
        }"
        p1BonusRubyLabel.text = "${
            rootService.gameService.getCurrentPlayer()
                .cards.count { it.bonus == Token.RUBY }
        }"
        p1BonusOnyxLabel.text = "${
            rootService.gameService.getCurrentPlayer()
                .cards.count { it.bonus == Token.ONYX }
        }"
        p1BonusEmeraldLabel.text = "${
            rootService.gameService.getCurrentPlayer()
                .cards.count { it.bonus == Token.EMERALD }
        }"
        p1BonusDiamondLabel.text = "${
            rootService.gameService.getCurrentPlayer()
                .cards.count { it.bonus == Token.DIAMOND }
        }"

        when (playersize) {
            2 -> {
                // Player at the top
                p3BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize].cards
                        .count { it.bonus == Token.SAPPHIRE }
                }"
                p3BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize].cards
                        .count { it.bonus == Token.RUBY }
                }"
                p3BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize].cards
                        .count { it.bonus == Token.ONYX }
                }"
                p3BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize].cards
                        .count { it.bonus == Token.EMERALD }
                }"
                p3BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize].cards
                        .count { it.bonus == Token.DIAMOND }
                }"

                p3PlayerBonusGridPlus.visual = ImageVisual("paper2.png")

                p3PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                createCardViewForStacks(
                    game.players[game.currentPlayer].reservedCards,
                    reservedCardsCurrentPlayer,
                    true
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 1) % playersize].reservedCards,
                    reserveCardPlayerOnTop,
                    0
                )
                p1PlayerLabel.text =
                    game.players[game.currentPlayer].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(game.players[game.currentPlayer])
                p3PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 1) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 1) % playersize]
                            )
            }
            3 -> {
                createCardViewForStacks(
                    game.players[game.currentPlayer].reservedCards,
                    reservedCardsCurrentPlayer,
                    true
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 1) % playersize].reservedCards,
                    reserveCardPlayerLeft,
                    0
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 2) % playersize].reservedCards,
                    reserveCardPlayerRight,
                    0
                )

                // Position 2 on the left side
                p2BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.SAPPHIRE }
                }"
                p2BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.RUBY }
                }"
                p2BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.ONYX }
                }"
                p2BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.EMERALD }
                }"
                p2BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.DIAMOND }
                }"
                p2PlayerBonusGridPlus.visual = ImageVisual("paper.png")

                p2PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                // P4 is on the right side
                p4BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.SAPPHIRE }
                }"
                p4BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.RUBY }
                }"
                p4BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.ONYX }
                }"
                p4BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.EMERALD }
                }"
                p4BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.DIAMOND }
                }"
                p4PlayerBonusGridPlus.visual = ImageVisual("paper2.png")

                p4PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                p1PlayerLabel.text =
                    game.players[game.currentPlayer].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(game.players[game.currentPlayer])
                p2PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 1) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 1) % playersize]
                            )
                p4PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 2) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 2) % playersize]
                            )
            }
            4 -> {
                createCardViewForStacks(
                    game.players[game.currentPlayer].reservedCards,
                    reservedCardsCurrentPlayer,
                    true
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 1) % playersize].reservedCards,
                    reserveCardPlayerLeft,
                    0
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 2) % playersize].reservedCards,
                    reserveCardPlayerOnTop,
                    0
                )
                createViewForReserved(
                    game.players[(game.currentPlayer + playersize - 3) % playersize].reservedCards,
                    reserveCardPlayerRight,
                    0
                )

                // P2 is the player on the left side
                p2BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.SAPPHIRE }
                }"
                p2BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.RUBY }
                }"
                p2BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.ONYX }
                }"
                p2BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.EMERALD }
                }"
                p2BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 1) % playersize]
                        .cards.count { it.bonus == Token.DIAMOND }
                }"
                p2PlayerBonusGridPlus.visual = ImageVisual("paper.png")

                p2PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                // P3 is on the top
                p3BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.SAPPHIRE }
                }"
                p3BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.RUBY }
                }"
                p3BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.ONYX }
                }"
                p3BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.EMERALD }
                }"
                p3BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 2) % playersize]
                        .cards.count { it.bonus == Token.DIAMOND }
                }"
                p3PlayerBonusGridPlus.visual = ImageVisual("paper.png")

                p3PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                // P4 is on the right side
                p4BonusSapphireLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 3) % playersize]
                        .cards.count { it.bonus == Token.SAPPHIRE }
                }"
                p4BonusRubyLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 3) % playersize]
                        .cards.count { it.bonus == Token.RUBY }
                }"
                p4BonusOnyxLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 3) % playersize]
                        .cards.count { it.bonus == Token.ONYX }
                }"
                p4BonusEmeraldLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 3) % playersize]
                        .cards.count { it.bonus == Token.EMERALD }
                }"
                p4BonusDiamondLabel.text = "${
                    game.players[(game.currentPlayer + playersize - 3) % playersize]
                        .cards.count { it.bonus == Token.DIAMOND }
                }"
                p4PlayerBonusGridPlus.visual = ImageVisual("paper.png")

                p4PlayerBonusGrid.apply {
                    set(2, 2, rubyPic)
                    set(2, 3, emeraldPic)
                    set(2, 4, sapphirePic)
                    set(2, 5, onyxPic)
                    set(2, 6, diamondPic)
                }

                p1PlayerLabel.text =
                    game.players[game.currentPlayer].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[game.currentPlayer]
                            )
                p2PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 1) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 1) % playersize]
                            )
                p3PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 2) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 2) % playersize]
                            )
                p4PlayerLabel.text =
                    game.players[(game.currentPlayer + playersize - 3) % playersize].name + "\nPunkte : " +
                            rootService.gameService.calculatePoints(
                                game.players[(game.currentPlayer + playersize - 3) % playersize]
                            )

            }

        }
    }


    private fun createCardViewForStacks(
        cardStack: List<DevelopmentCard>,
        linearLayout: LinearLayout<CardView>,
        reserved: Boolean
    ) {
        cardStack.forEach { card ->
            val cardView = CardView(
                posX = 0,
                posY = 0,
                130,
                200,
                front = ImageVisual(devLoad.createCompleteCardImage(card)),
                back = ImageVisual(devLoad.backImageForTier(card.tier))
            )
            cardView.onMouseClicked = {
                createButtonsForCards(card, card.tier, linearLayout.components.indexOf(cardView), reserved)
            }
            cardView.showFront()
            linearLayout.add(cardView)
        }
    }

    private fun loadTokenChipsVisual(token: Token): ImageVisual {
        val offset = when (token) {
            Token.ONYX -> 0
            Token.SAPPHIRE -> 106
            Token.DIAMOND -> 213
            Token.EMERALD -> 320
            Token.RUBY -> 427
            Token.GOLD -> 533
        }
        return ImageVisual("chips.png", width = 106, offsetX = offset)
    }

    private fun selectNoble() {
        val gameService = rootService.gameService
        gameService.getAvailableNobles().forEachIndexed { index, nobleTile ->
            val cardView = CardView(
                0,
                0,
                180,
                180,
                front = ImageVisual(nobLoad.createCompleteNobleImage(nobleTile))
            ).apply {
                onMouseClicked = {
                    createButtonsForNobleSelect(nobleTile, index)
                }
            }
            nobleSelection.add(cardView)
        }
        nobleSelection.isVisible = true
    }

    private fun initializeStack(stack: List<DevelopmentCard>, stackView: CardStack<CardView>) {
        stack.forEach { card ->
            val cardView = CardView(
                height = 200,
                width = 130,
                front = ImageVisual(devLoad.createCompleteCardImage(card)),
                back = ImageVisual(devLoad.backImageForTier(card.tier))
            ).apply {
                onMouseClicked = {
                    createButtonsForStack(card, card.tier)
                }
            }
            stackView.add(cardView)
        }
    }

    private fun createNobleView(
        nobleStack: List<NobleTile>,
        stackView1: LinearLayout<CardView>,
        stackView2: LinearLayout<CardView>
    ) {
        nobleStack.forEach { card ->
            val cardView = CardView(
                height = 180,
                width = 180,
                front = ImageVisual(nobLoad.createCompleteNobleImage(card)),
            )
            if (nobleCount < 3) {
                stackView1.add(cardView)
            }
            if (nobleCount >= 3) {
                stackView2.add(cardView)
            }
            nobleCount++
        }
    }

    private fun createButtonsForCards(card: DevelopmentCard, tier: Int, column: Int, reserved: Boolean) {
        targetedCard = card
        if (!reserved) {
            reserveButtonForCards.posX = tier3Cards.posX + ((column) * (130 + tier3Cards.spacing))
            reserveButtonForCards.posY = tier3Cards.posY - reserveButtonForCards.height - 2 + (3 - tier) * 231
            buyButtonForCards.posX = reserveButtonForCards.posX
            buyButtonForCards.posY = reserveButtonForCards.posY + reserveButtonForCards.height + tier3Stack.height + 4
            checkButtonStateForCards(reserveButtonForCards, buyButtonForCards)
        } else {
            buyButtonForCards.posX =
                reservedCardsCurrentPlayer.posX + ((column) * (130 + reservedCardsCurrentPlayer.spacing))
            buyButtonForCards.posY = reservedCardsCurrentPlayer.posY - 30
            checkButtonStateForStack(buyButtonForCards)
        }


    }

    private fun createButtonsForStack(card: DevelopmentCard, tier: Int) {
        targetedCard = card
        reserveButtonForStack.posX = tier3Stack.posX
        reserveButtonForStack.posY = tier3Stack.posY - reserveButtonForCards.height - 2 + (3 - tier) * 231
        checkButtonStateForStack(reserveButtonForStack)
    }

    /**
     * Moves the Token from one Stack to another, only if there are tokens left
     *
     */
    private fun moveTokenView(from: LinearLayout<CardView>, to: LinearLayout<CardView>) {
        if (from.isEmpty()) return
        val tokenViewToMove = from.last()
        println("start Moving")

        //Funktioniert leider nicht
//        playAnimation(
//            MovementAnimation.toComponentView(
//                tokenViewToMove,
//                to,
//                this,
//                duration = 1000
//            ).apply {
//                onFinished = {
//                    tokenViewToMove.removeFromParent()
//                    tokenViewToMove.rotate((0..359).random())
//                    to.add(tokenViewToMove)
//                    refreshOpacityOnGemStackView(to)
//                    refreshOpacityOnGemStackView(from)
//                    println("finished Moving")
//                }
//            })
        tokenViewToMove.removeFromParent()
        tokenViewToMove.rotate((0..359).random())
        to.add(tokenViewToMove)
        refreshOpacityOnGemStackView(to)
        refreshOpacityOnGemStackView(from)
        println("finished Moving")
    }


    private fun createTokenStackView(tokens: List<Token>, tokenToLinearLayoutMap: Map<Token, LinearLayout<CardView>>) {
        for (token in tokens) {
            val tokenView = CardView(
                width = 50,
                height = 50,
                front = loadTokenChipsVisual(token)
            )
            tokenToLinearLayoutMap.getValue(token).add(tokenView)
        }
        tokenToLinearLayoutMap.filterKeys { tokens.contains(it) }.values.toList().forEach { mixTokenView(it) }
    }

    //Shuffles opacity and Rotation of each element
    //Only for freshly created TokenViews
    private fun mixTokenView(gemStackView: LinearLayout<CardView>) {
        gemStackView.forEach { it.rotate((0..359).random()) }
        refreshOpacityOnGemStackView(gemStackView)
    }

    private fun refreshOpacityOnGemStackView(gemStackView: LinearLayout<CardView>) {
        if (gemStackView.isNotEmpty()) {
            var index = 1
            val gemStackSize = gemStackView.count { true }
            for (gem in gemStackView) {
                gem.opacity = 0.8 + (index - gemStackSize) * 0.05
                index++
            }
            gemStackView.last().opacity = 1.0
        }
    }


    private fun createButtonsForTokens(token: Token, index: Int) {
        targetedToken = token
        when (index) {
            0 -> {
                takeOneButtonForTokens.posX = 1250.0
                takeOneButtonForTokens.posY = 190.0
                takeTwoButtonForTokens.posX = 1250.0
                takeTwoButtonForTokens.posY = 240.0
                checkTokenState()
            }
            1 -> {
                takeOneButtonForTokens.posX = 1250.0
                takeOneButtonForTokens.posY = 300.0
                takeTwoButtonForTokens.posX = 1250.0
                takeTwoButtonForTokens.posY = 350.0
                checkTokenState()
            }
            2 -> {
                takeOneButtonForTokens.posX = 1250.0
                takeOneButtonForTokens.posY = 400.0
                takeTwoButtonForTokens.posX = 1250.0
                takeTwoButtonForTokens.posY = 450.0
                checkTokenState()
            }
            3 -> {
                takeOneButtonForTokens.posX = 1250.0
                takeOneButtonForTokens.posY = 505.0
                takeTwoButtonForTokens.posX = 1250.0
                takeTwoButtonForTokens.posY = 555.0
                checkTokenState()
            }
            4 -> {
                takeOneButtonForTokens.posX = 1250.0
                takeOneButtonForTokens.posY = 610.0
                takeTwoButtonForTokens.posX = 1250.0
                takeTwoButtonForTokens.posY = 660.0
                checkTokenState()
            }


        }
    }

    private fun createButtonsForNobleSelect(nobleTile: NobleTile, index: Int) {
        targetedNoble = nobleTile
        nobleTargeted = true
        nobleSelectButton.posX = nobleSelection.posX + (index * 180) + nobleSelection.spacing
        nobleSelectButton.posY = nobleSelection.posY - 40
        checkButtonStateForNobles(nobleSelectButton)
    }


    /**
     * checkMethods
     */

    private fun checkButtonStateForNobles(button: Button) {
        if (allowToClickNoble) {
            button.isVisible = true
            allowToClickNoble = false
        } else {
            button.isVisible = false
            allowToClickNoble = true
        }

    }

    private fun checkButtonStateForStack(button: Button) {
        if (allowToClickStack) {
            if (!cardViewStatus) {
                button.isVisible = true
                cardViewStatus = true
                allowToClickToken = false
                allowToClickCard = false
                allowToClickNoble = false
            } else {
                button.isVisible = false
                cardViewStatus = false
                allowToClickToken = true
                allowToClickCard = true
                allowToClickNoble = true
            }
        }
    }

    private fun checkTokenState() {
        if (allowToClickToken) {
            if (!tokenViewStatus) {
                takeOneButtonForTokens.isVisible = true
                takeTwoButtonForTokens.isVisible = true
                tokenViewStatus = true
                allowToClickStack = false
                allowToClickCard = false
                allowToClickNoble = false
            } else {
                takeOneButtonForTokens.isVisible = false
                takeTwoButtonForTokens.isVisible = false
                tokenViewStatus = false
                allowToClickStack = true
                allowToClickCard = true
                allowToClickNoble = false
            }
        }
    }

    //Completely loads new TokenView
    private fun refreshTokenView() {
        playerToLayoutContainer.forEach { (_, v) -> v.forEach { it.clear() } }
        rootService.currentGame.getCurrentGame().players.forEachIndexed { index, player ->
            val playerTokensList = mutableListOf<Token>()
            player.tokens.forEach { (k, v) ->
                repeat(v) { playerTokensList.add(k) }
            }
            createTokenStackView(playerTokensList, playerToTokenToLinearLayout[index]!!)
        }
        gameTokenContainer.forEach { it.clear() }
        val tokenList = mutableListOf<Token>()
        rootService.currentGame.getCurrentGame().tokens.forEach { (k, v) ->
            repeat(v) { tokenList.add(k) }
        }
        createTokenStackView(tokenList, gameTokenToLinearLayout)
    }

    private fun checkButtonStateForCards(button1: Button, button2: Button) {
        if (allowToClickCard) {
            if (!cardViewStatus) {
                button1.isVisible = true
                button2.isVisible = true
                cardViewStatus = true
                allowToClickToken = false
                allowToClickStack = false
            } else {
                button1.isVisible = false
                button2.isVisible = false
                cardViewStatus = false
                allowToClickToken = true
                allowToClickStack = true
            }
        }
    }

    private fun clearAllAndDisableButtons() {
        tier1Stack.clear()
        tier2Stack.clear()
        tier3Stack.clear()
        tier1Cards.clear()
        tier2Cards.clear()
        tier3Cards.clear()
        nobleLayout1.clear()
        nobleLayout2.clear()
        reservedCardsCurrentPlayer.clear()
        reserveCardPlayerLeft.clear()
        reserveCardPlayerOnTop.clear()
        reserveCardPlayerRight.clear()
        tokenViewLayout.clear()
        targetedTokenList.clear()
        nobleSelection.clear()
        nobleSelection.isVisible = false
        nobleSelectButton.isVisible = false
        hintLabel.isVisible = false
        hintButton.isVisible = true
        reserveButtonForStack.isVisible = false
        buyButtonForCards.isVisible = false
        reserveButtonForCards.isVisible = false
        takeOneButtonForTokens.isVisible = false
        takeTwoButtonForTokens.isVisible = false
        tookToken = false
        allowToClickStack = true
        allowToClickCard = true
        allowToClickToken = true
        nobleCount = 0
        tokenCount = 0
    }

    private fun createAllCards() {
        val game = rootService.currentGame.getCurrentGame()
        initializeStack(game.drawCards[0], tier1Stack)
        initializeStack(game.drawCards[1], tier2Stack)
        initializeStack(game.drawCards[2], tier3Stack)
        createCardViewForStacks(game.openCards[0], tier1Cards, false)
        createCardViewForStacks(game.openCards[1], tier2Cards, false)
        createCardViewForStacks(game.openCards[2], tier3Cards, false)
        createPlayerView(game.players.size)
        createNobleView(game.nobleTiles, nobleLayout1, nobleLayout2)
        createMoveHistory(rootService.currentGame.gameHistory)
    }

    private fun tryTokenDraw(): Boolean {
        return try {
            action.selectTokens(targetedTokenList)
            true
        } catch (_: Exception) {
            println("Turn not allowed")
            targetedTokenList.clear()
            refreshTokenView()
            tookToken = false
            tokenCount = 0
            tokenDrawNotAllowed.isVisible = true
            playAnimation(DelayAnimation(2000).apply { onFinished = { tokenDrawNotAllowed.isVisible = false } })
            false
        }
    }

    private fun takeOneToken() {
        val game = rootService.currentGame.getCurrentGame()
        if (!tookToken) {
            targetedTokenList.add(targetedToken)

            moveTokenView(
                gameTokenToLinearLayout[targetedToken]!!,
                playerToTokenToLinearLayout[game.currentPlayer]!![targetedToken]!!
            )

            tookToken = true
            tokenCount++
            takeOneButtonForTokens.isVisible = false
            takeTwoButtonForTokens.isVisible = false
        } else {
            if (targetedTokenList.contains(targetedToken) && tokenCount < 2) {
                targetedTokenList.add(targetedToken)

                moveTokenView(
                    gameTokenToLinearLayout[targetedToken]!!,
                    playerToTokenToLinearLayout[game.currentPlayer]!![targetedToken]!!
                )
                if (!tryTokenDraw()) return
                currentYielder = rootService.gameService.startNextRound()

            } else if (targetedTokenList.contains(targetedToken) && tokenCount == 2) {
                println("Keine Doppelten Tokens mehr moeglich")
            } else {
                moveTokenView(
                    gameTokenToLinearLayout[targetedToken]!!,
                    playerToTokenToLinearLayout[
                            rootService.currentGame.getCurrentGame().currentPlayer]!![targetedToken]!!
                )
                if (tokenCount == 2) {
                    targetedTokenList.add(targetedToken)
                    if (!tryTokenDraw()) return
                    currentYielder = rootService.gameService.startNextRound()
                } else {
                    targetedTokenList.add(targetedToken)
                    tokenCount++
                }
            }
        }
    }


    private fun takeTwoTokens() {
        if (!tookToken) {
            targetedTokenList.add(targetedToken)
            targetedTokenList.add(targetedToken)

            listOf(targetedToken, targetedToken).forEach {
                moveTokenView(
                    gameTokenToLinearLayout[it]!!,
                    playerToTokenToLinearLayout[rootService.currentGame.getCurrentGame().currentPlayer]!![it]!!
                )
            }
            if (!tryTokenDraw()) return
            currentYielder = rootService.gameService.startNextRound()
        }
    }

    private fun refreshPlayerTokenRotationAndPosition() {
        val game = rootService.currentGame.getCurrentGame()
        val currentPlayer = game.currentPlayer
        val players = game.players
        val playerSize = players.size
        playerToLayoutContainer.forEach {
            var currentPosition = it.key - currentPlayer
            if (currentPosition < 0) {
                currentPosition += playerSize
            }
            it.value.changeOrientationTo(
                playerTokenOrientation[currentPosition].first,
                playerTokenOrientation[currentPosition].second
            )
            it.value.move(posX = playerToPosX[currentPosition], posY = playerToPosY[currentPosition])
        }
    }


    private fun createTokenSelectButton(token: Token, tokenStack: LinearLayout<CardView>) {
        takeTokenForBuy.posX = tokenStack.posX - 25
        takeTokenForBuy.posY = tokenStack.posY + 50


        checkButtonStateForStack(takeTokenForBuy)

        println(takeTokenForBuy.isVisible)
        takeTokenForBuy.onMouseClicked = {
            println(buyCardTokenMap.toString())
            gameTokenToLinearLayout[token]?.let { it1 -> moveTokenView(tokenStack, it1) }
            confirmBuyButton.isVisible = true
            val count = buyCardTokenMap.getOrDefault(token, 0) + 1
            buyCardTokenMap[token] = count
            println(buyCardTokenMap.toString())
        }
    }

    private fun discardTokens() {
        //Check if 10 Tokens are left
        println(targetedTokenList.size)
        selectDiscardTokenButton.isVisible = false
        try {
            action.returnTokens(targetedTokenList)
        } catch (e: IllegalStateException) {
            //TODONE: print error message
            if (e.localizedMessage == "Detekted") {
                println("We were detekted")
            }
            targetedTokenList.clear()
            println("error Liste")
            refreshTokenView()
            return
        }
        currentYielder?.continueWith(null)

    }

    private fun createTokenSelectButtonForDiscard(token: Token, tokenStack: LinearLayout<CardView>) {
        selectDiscardTokenButton.posX = tokenStack.posX - 25
        selectDiscardTokenButton.posY = tokenStack.posY + 50


        checkButtonStateForStack(selectDiscardTokenButton)

        println(selectDiscardTokenButton.isVisible)
        selectDiscardTokenButton.onMouseClicked = {
            println(targetedTokenList.toString())
            gameTokenToLinearLayout[token]?.let { it1 -> moveTokenView(tokenStack, it1) }
            confirmDiscardTokenButton.isVisible = true
            targetedTokenList.add(token)
            println(targetedTokenList.toString())
        }
    }

    private fun makeTokenSelectionAvailableForDiscard() {
        playerToTokenToLinearLayout[rootService.currentGame.getCurrentGame().currentPlayer]?.forEach {
            if (it.value.isNotEmpty()) it.value.onMouseClicked = { _ ->
                createTokenSelectButtonForDiscard(it.key, it.value)
            }
        }
    }

    private fun makeTokenSelectionAvailable() {
        playerToTokenToLinearLayout[rootService.currentGame.getCurrentGame().currentPlayer]?.forEach {
            if (it.value.isNotEmpty()) it.value.onMouseClicked = { _ ->
                createTokenSelectButton(it.key, it.value)
            }
        }
    }

    private fun buyCard() {
        val sourceStack: SourceStack =
            if (rootService.gameService.getCurrentPlayer().reservedCards.contains(targetedCard)) {
                SourceStack.RESERVED
            } else {
                SourceStack.open(targetedCard.tier)
            }
        try {
            action.buyCard(targetedCard, buyCardTokenMap.toTokenMap(), sourceStack)
        } catch (e: IllegalStateException) {
            //TODONE: Print Message on GUI
            if (e.localizedMessage == "Detekted") {
                println("We were detekted")
            }
            println("Buying not allowed")
            refreshTokenView()
            buyCardTokenMap.clear()
            return
        } catch (e: IllegalArgumentException) {
            //TODONE: Print Message on GUI
            if (e.localizedMessage == "Detekted!") {
                println("We were detekted! Oops")
            }
            println("Buying not allowed")
            refreshTokenView()
            buyCardTokenMap.clear()
            return
        }
        currentYielder = rootService.gameService.startNextRound()

    }


    /**
     * Refreshables
     */
    override fun refreshAfterTurn(turnType: TurnType) {
        currentYielder = null
        println("Hallo refreshAfterTurn")
        clearAllAndDisableButtons()
        createAllCards()
        refreshTokenView()
        refreshPlayerTokenRotationAndPosition()

        val currentPlayer = rootService.gameService.getCurrentPlayer()
        if (currentPlayer.playerType != PlayerType.HUMAN) {
            val ai = when (currentPlayer.playerType) {
                PlayerType.EASY -> SimpleAI()
                PlayerType.MEDIUM -> MediumAI()
                PlayerType.HARD -> HardAI()
                else -> SimpleAI()
            }

            val move = ai.determineBestMove(rootService.currentGame.getCurrentGame(), 10000)

            // Speed abhängig von Toggle
            var speed = 2800
            if (botSpeedToggle.selectedProperty.value) {
                speed = 150
            }

            lock()
            playAnimation(DelayAnimation(speed).apply { onFinished = { move.perform(rootService); unlock() } })


        }

    }

    override fun refreshAfterCardReserved(card: DevelopmentCard, source: SourceStack) {
        clearAllAndDisableButtons()
        createAllCards()
    }

    override fun refreshAfterGameFinished(endType: Int) {
        rootService.gameService.saveHighScore()
        splendorApp.highscoreScene.highScoreJSON = rootService.gameService.loadHighScoreList()
        splendorApp.highscoreScene.loadHighscore()
        val finishedGameScene = FinishedGameScene(rootService)
        splendorApp.showMenuScene(finishedGameScene)

        finishedGameScene.apply {
            quitButton.onMouseClicked = {
                splendorApp.exit()
            }
            highscoreButton.onMouseClicked = {
                splendorApp.showMenuScene(splendorApp.highscoreScene)
            }
        }
    }

    override fun refreshAfterNobleVisit(nobleTile: NobleTile) {
        clearAllAndDisableButtons()
        createAllCards()
    }

    override fun refreshBeforeTokenDiscard() {
        targetedTokenList.clear()
        makeTokenSelectionAvailableForDiscard()
    }

    override fun refreshBeforeNobleVisit(nobleTiles: List<NobleTile>) {
        selectNoble()
    }

    override fun refreshAfterCardBought(card: DevelopmentCard, stack: SourceStack) {
        clearAllAndDisableButtons()
        createAllCards()
    }

    override fun refreshAfterTokenDiscard(tokens: List<Token>) {
        clearAllAndDisableButtons()
        createAllCards()
    }

    override fun refreshAfterTokenDrawn() {
        clearAllAndDisableButtons()
        createAllCards()
    }

}