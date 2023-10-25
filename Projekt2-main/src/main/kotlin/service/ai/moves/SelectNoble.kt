package service.ai.moves

import entity.NobleTile

/**
 * This special move is used when a player has the option to select on of multiple nobles
 *
 * @param noble The noble the player wants to select
 */
data class SelectNoble(val noble: NobleTile) {
    val logMessage = "select noble"
}