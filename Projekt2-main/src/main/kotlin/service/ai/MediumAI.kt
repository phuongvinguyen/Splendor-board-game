package service.ai

import entity.GameManager
import entity.GameState
import entity.Token
import service.RootService
import service.ai.AI.Companion.TOKEN_COMBINATIONS
import service.ai.moves.*
import kotlin.concurrent.thread

// TODONE: many cpus provide two threads per core. yes they do you are right
val numCpus = Runtime.getRuntime().availableProcessors()

/**
 * Represents a medium difficulty AI for Splendor that utilizes
 * a mixture of random moves and an implementation of the
 * Monte Carlo tree search (MCTS) algorithm.
 */
open class MediumAI : AI {
    /**
     * Determines the best of the four possible
     * moves to take for the current player of [gameState]
     * by running a Monte Carlo tree search and always taking
     * a random choice whenever prompted to. When [maxTimeMs]
     * elapses after a call of this method, the simulation is immediately
     * cancelled. Winning the game is preferred (regardless of the
     * conditions or the time it may take to reach a conclusion).
     * If no win could be attained, a draw is valued higher than
     * a loss.
     *
     * Note that all subsequent actions taken in the simulation
     * are randomly chosen, therefore the returned move most definitely
     * is not the objectively best to take.
     */
    override fun determineBestMove(gameState: GameState, maxTimeMs: Long): Move {
        val root = expandTreeMultithreaded(gameState.copy(), maxTimeMs)

        val bestNode = root.getBestChild(gameState.currentPlayer)
        println("${bestNode?.winRate(gameState.currentPlayer)}")
        return bestNode?.move ?: Pass(getSelectNobleMove(gameState))
    }

    /**
     * This function performs the tree expansion in a multi-threaded way
     * TODONE: Synchronize every second or so
     *
     * @param gameState The root game state
     * @param maxTimeMs The maximum amount of milliseconds the AI is allowed to use
     * @return The mcs tree
     */
    open fun expandTreeMultithreaded(gameState: GameState, maxTimeMs: Long): MCSTreeNode {
        val copiedRootNodes =
            MutableList(numCpus) { MCSTreeNode(gameState.copy(), Pass(getSelectNobleMove(gameState))) }

        // TODONE: Find a better (maybe kotlin-based) solution. Found a kotlin-based solution
        /*val executor = Executors.newCachedThreadPool()
        for (rootNode in copiedRootNodes) {
            val worker = Runnable { expandTree(rootNode, maxTimeMs) }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) {
            try {
                Thread.sleep(5)
            } catch(e: InterruptedException) {
                e.printStackTrace()
            }
        }*/
        val workers: MutableList<Thread> = mutableListOf()
        for (rootNode in copiedRootNodes) {
            //val worker = Runnable { expandTree(rootNode, maxTimeMs) }
            //executor.execute(worker)
            workers += thread(start = true, isDaemon = true) {
                expandTree(rootNode, maxTimeMs)
            }
        }
        // wait for threads
        for (worker in workers) {
            worker.join()
        }

        val mainNode = copiedRootNodes.removeLast()
        for (node in copiedRootNodes) {
            mainNode.mergeWith(node)
        }
        return mainNode
    }

    /**
     * expands the Tree by adding random children(moves) to the tree
     * and propagating the outcome of the game when this move is preformed
     * @param root the current node we would like to expand from
     * @param maxTimeMs the maximum amount of time in milliseconds that the expansion has
     */
    private fun expandTree(root: MCSTreeNode, maxTimeMs: Long) {
        val rootService = RootService()
        val startTime = System.currentTimeMillis()
        var i = 0
        while (System.currentTimeMillis() - startTime < maxTimeMs) {
            val leaf = findLeafNode(root)

            rootService.currentGame = GameManager(leaf.game.copy())
            if (rootService.gameService.checkGameEnd()) {
                continue
            }

            expandChildren(leaf)

            i += 1

            // Complete a random game.
            // if no new child is available, play again with this leaf node
            val newChild: MCSTreeNode = if (leaf.children.isEmpty()) {
                leaf
            } else {
                leaf.children.values.random()
            }

            val winner = playToEnd(newChild, maxTimeMs)
            leaf.backPropagate(winner)
        }
        println("Performed $i (${root.children.size}) iterations in $maxTimeMs ms")
    }

    /**
     * Completes that game at the nodes state by performing random moves
     */
    private fun playToEnd(node: MCSTreeNode, maxTimeMs: Long): Int? {
        val ai = getPlayToEndAI()
        val service = RootService(node.game.copy())

        while (!service.gameService.checkGameEnd()) {
            val game = service.currentGame.getCurrentGame()
            val move = ai.determineBestMove(game, maxTimeMs)

            move.perform(service)

            val newGame = service.currentGame.getCurrentGame()
            if (newGame.passingCounter == newGame.players.size) {
                return null
            }
        }

        return service.gameService.getBestPlayerIndex()
    }

    /**
     * This method returns the AI that should be used when simulating games
     *
     * @return The AI
     */
    protected open fun getPlayToEndAI() = SimpleAI()

    /**
     * looks through the children of a node for a leaf node
     * @param root, the node we start from
     * @return a leaf node
     */
    open fun findLeafNode(root: MCSTreeNode): MCSTreeNode {
        var child: MCSTreeNode = root

        while (child.children.isNotEmpty()) {
            child = child.children.values.random()
        }
        return child
    }

    private fun expandChildren(leaf: MCSTreeNode) {
        addDrawTwoTokensMoves(leaf)
        addDrawThreeTokensMoves(leaf)
        addReserveCardMoves(leaf)
        addBuyCardMoves(leaf)
    }

    /**
     * This method adds all moves to the leaf node that draw two tokens
     * This excludes moves where a token would have to be put back, since these moves can
     * be performed using drawThreeToken moves
     */
    private fun addDrawTwoTokensMoves(leaf: MCSTreeNode) {
        val game = leaf.game
        val player = game.players[game.currentPlayer]

        if (player.totalTokens() >= 9) {
            return
        }

        val selectNobleMove = getSelectNobleMove(game)
        for ((token, amt) in leaf.game.tokens) {
            if (amt < 4 || token == Token.GOLD) {
                continue
            }

            val move = DrawTwoTokens(token, selectNobleMove, null)
            leaf.addMove(move)
        }
    }

    /**
     * Creates all children for the [leaf] node that perform the
     * move [DrawThreeTokens]. If a combination of the available
     * tokens is not possible to draw, this combination is
     * ignored.
     */
    private fun addDrawThreeTokensMoves(leaf: MCSTreeNode) {
        val player = leaf.game.players[leaf.game.currentPlayer]

        val availableMidTokens = leaf.game.tokens.count { (token, amt) ->
            amt > 0 && token != Token.GOLD
        }

        val selectNobleMove = getSelectNobleMove(leaf.game)
        for (combination in TOKEN_COMBINATIONS) {
            val availableTokensOfCombination = combination.filter { leaf.game.tokens[it] > 0 }
            if (availableTokensOfCombination.size < minOf(3, availableMidTokens)
                || availableTokensOfCombination.isEmpty()
            ) {
                continue
            }
            // in case of having too many tokens, random tokens are chosen to get
            // rid of
            val excessTokens = (player.totalTokens() + availableTokensOfCombination.size) - 10
            val returnTokensMove = if (excessTokens > 0) {
                val playerTokens = player.tokens.flatMap { (token, amt) -> List(amt) { token } }
                val selectedTokens = playerTokens.shuffled().take(excessTokens).toList()
                ReturnTokens(selectedTokens)
            } else {
                null
            }
            leaf.addMove(DrawThreeTokens(availableTokensOfCombination.toList(), selectNobleMove, returnTokensMove))
        }
    }


    /**
     * This method adds at most five moves to reserve some cards
     *
     * @param leaf The node to add the moves to
     */
    protected open fun addReserveCardMoves(leaf: MCSTreeNode) {
        val game = leaf.game
        val player = game.players[game.currentPlayer]

        if (player.reservedCards.size >= 3) {
            return
        }

        val reservableCards = getAvailableReserveCards(game)

        val selectNobleMove = getSelectNobleMove(game)
        for (card in reservableCards) {
            val returnTokensMove = if (player.totalTokens() >= 9) {
                // Return a random token, except the token that was drawn
                val tokenToReturn = player.tokens.flatMap { (token, amt) -> List(amt) { token } }.toList().random()
                ReturnTokens(listOf(tokenToReturn))
            } else {
                null
            }

            val move = ReserveCard(card.first, card.second, selectNobleMove, returnTokensMove)
            leaf.addMove(move)
        }
    }

    /**
     * This method adds all possible moves in which a card is bought
     * The payment is always minimal and tries to not use gold
     *
     * @param leaf The node to add the moves to
     */
    private fun addBuyCardMoves(leaf: MCSTreeNode) {
        val game = leaf.game
        val player = game.players[game.currentPlayer]

        val affordableCards = getAffordableCards(game, player)
        for (card in affordableCards) {
            val payment = getMinimumPayment(player, card.first)
            val selectNobleMove = getSelectNobleMove(game, listOfNotNull(card.first.bonus))

            val move = BuyCard(card.first, card.second, payment, selectNobleMove)
            leaf.addMove(move)
        }
    }

    private fun getSelectNobleMove(game: GameState, bonuses: List<Token> = emptyList()): SelectNoble? {
        val availableNobles = getAvailablePlayerNobles(game, game.players[game.currentPlayer], bonuses)
        return if (availableNobles.size >= 2) {
            SelectNoble(availableNobles.random())
        } else {
            null
        }
    }


}

