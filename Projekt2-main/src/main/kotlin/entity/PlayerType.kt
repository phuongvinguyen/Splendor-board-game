package entity

/**
 * Enum that distinguish between the four player types.
 *
 * A player can be controlled by a human (HUMAN) or a bot. The bot is represented by its difficulty
 * levels EASY, MEDIUM and HARD.
 */

enum class PlayerType {
    HUMAN,
    EASY,
    MEDIUM,
    HARD
}