package service.ai

import entity.GameState
import service.RootService
import service.ai.moves.Move
import kotlin.math.ln
import kotlin.math.sqrt

// The theoretical optimum for this value is sqrt(2), but lower values prefer exploitation to exploration
const val EXPLORATION_FACTOR: Double = 1.25 // sqrt(2)

/**
 * A monte-carlo search tree used by the AI to search trough game state space
 */
data class MCSTreeNode(
    val game: GameState,
    val move: Move,
    val children: MutableMap<Move, MCSTreeNode> = mutableMapOf(),
    private var parent: MCSTreeNode? = null,
    var playedGames: Int = 0,
    var wonGames: MutableList<Float> = mutableListOf(0f, 0f, 0f, 0f)
) {
    /**
     * Returns the current win rate of this move based
     * on random subsequent playouts.
     * @param player the index of the player
     * @return The percentage of this move to win
     */
    fun winRate(player: Int) = if (playedGames == 0) {
        0f
    } else {
        wonGames[player] / playedGames
    }

    /**
     * This method adds a child to this node
     *
     * @param child The child to add
     */
    private fun addChild(child: MCSTreeNode) {
        val previousChild = children[child.move]
        if (previousChild == null) {
            child.parent = this
            children[child.move] = child
        } else {
            previousChild.mergeWith(child)
        }
    }

    /**
     * Performs this [move] on the current game state and
     * subsequently adds the [move] as a child to this node.
     *
     * @param move the move to add to this node
     */
    fun addMove(move: Move) {
        val childGame = move.performOnGameState(game)
        addChild(MCSTreeNode(childGame, move))
    }

    /**
     * Updates all of this node's parents to reflect the
     * changes in win rate for the [winning][winner] player.
     * @param winner the index of the player that won the
     * playout
     */
    fun backPropagate(winner: Int?) {
        var scores = MutableList(game.players.size) { 0f }
        if (winner == null) {
            scores = MutableList(game.players.size) { 0.5f }
        } else {
            scores[winner] = 1f
        }

        var node: MCSTreeNode? = this
        while (node != null) {
            node.playedGames += 1
            for (i in scores.withIndex()) {
                node.wonGames[i.index] += i.value
            }

            node = node.parent
        }

    }

    /**
     * Returns the child (move) that is most likely and quickest to win the game
     * for the player identified by [index][player].
     * @param player the player's index whose most optimal move should be
     * returned
     * @return the child that is both most likely to win the game for [player]
     */
    fun getBestChild(player: Int): MCSTreeNode? {
        val guaranteedWin = children.values.find { it.isWinFor(player) }
        if (guaranteedWin != null) {
            return guaranteedWin
        }
//        for (child in children.values) {
//            val wr = DecimalFormat("#.###").format(child.winRate(this.game.currentPlayer))
//            println("${child.playedGames} (WR: ${wr}) - ${child.move}")
//        }
        return children.values.maxByOrNull { it.playedGames }
    }


    /**
     * Returns the Upper Confidence Bound for trees
     * (UCT) for this node and [player]
     * @param player the index of the player
     */
    fun getUct(player: Int) =
        winRate(player) + EXPLORATION_FACTOR * sqrt(ln(parent?.playedGames?.toDouble() ?: 0.0) / playedGames)

    /**
     * This method returns true if the current player is guaranteed to win with this move
     *
     * @param playerIdx The index of the player to check for
     * @return true, if the player will win with this move
     */
    private fun isWinFor(playerIdx: Int): Boolean {
        val service = RootService(game)
        val player = game.players[playerIdx]
        return service.gameService.getBestPlayerIndex() == playerIdx && service.gameService.calculatePoints(player) >= 15
    }

    /**
     * Merges this node with [other] and updates the data
     * of all shared nodes. The parent attribute is never
     * updated, so this method only works on a root node.
     * @param other the other node to merge with
     */
    fun mergeWith(other: MCSTreeNode) {
        this.playedGames += other.playedGames
        for (i in this.wonGames.indices) {
            this.wonGames[i] += other.wonGames[i]
        }
        for (childOfSecond in other.children.values) {
            addChild(childOfSecond)
        }
    }
}