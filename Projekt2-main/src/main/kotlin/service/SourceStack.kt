package service

/**
 * This enum describes the location where a card lies.
 * This is used mainly to animate the card movement in the gui
 */
enum class SourceStack {
    RESERVED,
    OPEN_TIER1,
    OPEN_TIER2,
    OPEN_TIER3,
    DRAW_TIER1,
    DRAW_TIER2,
    DRAW_TIER3;

    /**
     * @return whether this source stack is part of the open stacks
     */
    fun isOpen() = when (this) {
        OPEN_TIER1, OPEN_TIER2, OPEN_TIER3 -> true
        else -> false
    }

    /**
     * @return whether this source stack is part of the draw stacks
     */
    fun isDraw() = when (this) {
        DRAW_TIER1, DRAW_TIER2, DRAW_TIER3 -> true
        else -> false
    }

    /**
     * @return The index of the stack described by this enum
     */
    fun getIndex() = when (this) {
        OPEN_TIER1, DRAW_TIER1 -> 0
        OPEN_TIER2, DRAW_TIER2 -> 1
        OPEN_TIER3, DRAW_TIER3 -> 2
        RESERVED -> throw IllegalStateException("Reserved card stack has no index")
    }

    companion object {
        /**
         * Returns the open stack with the given tier
         * @param tier The tier of the draw stack
         * @throws IllegalStateException if the tier is not within 1 and 3
         */
        fun open(tier: Int) = when (tier) {
            1 -> OPEN_TIER1
            2 -> OPEN_TIER2
            3 -> OPEN_TIER3
            else -> throw IllegalStateException("tier must be between 1 and 3")
        }

        /**
         * Returns the draw stack with the given tier
         * @param tier The tier of the draw stack
         * @throws IllegalStateException if the tier is not within 1 and 3
         */
        fun draw(tier: Int) = when (tier) {
            1 -> DRAW_TIER1
            2 -> DRAW_TIER2
            3 -> DRAW_TIER3
            else -> throw IllegalStateException("tier must be between 1 and 3")
        }
    }
}