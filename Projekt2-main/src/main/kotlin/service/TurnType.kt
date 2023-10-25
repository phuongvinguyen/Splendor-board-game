package service

/**
 * This enum describes what the 'kind' of a turn is.
 * A turn can either be a normal turn, a rollback to a previous turn, or a redo
 */
enum class TurnType {
    NORMAL, UNDO, REDO
}