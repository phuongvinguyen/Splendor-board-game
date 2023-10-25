package entity

/**
 * Enum to distinguish between the six available token types in a Splendor game.
 */
enum class Token(val translation: String) {
    ONYX("ONYX"),
    SAPPHIRE("SAPHIR"),
    DIAMOND("DIAMANT"),
    EMERALD("SMARAGD"),
    RUBY("RUBIN"),
    GOLD("GOLD");

    /**
     * Converts the Token values to Strings
     */
    override fun toString() =
        when (this) {
            ONYX -> "Onyx"
            SAPPHIRE -> "Sapphire"
            DIAMOND -> "Diamond"
            EMERALD -> "Emerald"
            RUBY -> "Ruby"
            GOLD -> "Gold"
        }
}