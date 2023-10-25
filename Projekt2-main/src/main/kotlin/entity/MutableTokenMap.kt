package entity

import kotlinx.serialization.Serializable

/**
 * This class is used to represent a mutable collection of tokens.
 * See also: [TokenMap]
 *
 * @param tokens The backing array of tokens
 */
@Serializable
class MutableTokenMap(val tokens: IntArray = IntArray(Token.values().size) { 0 }) : TokenMap(),
    Sequence<Pair<Token, Int>> {

    /**
     * This method increases the number of tokens of a specific kind by the specified amount
     *
     * @param token The kind of token
     * @param amount The amount to increase by
     */
    fun add(token: Token, amount: Int = 1) {
        tokens[token.ordinal] += amount
    }

    /**
     * This method reduces the number of tokens of a specific kind by the specified amount
     *
     * @param token The kind of token
     * @param amount The amount to decrease by
     */
    fun remove(token: Token, amount: Int = 1) {
        tokens[token.ordinal] -= amount
    }

    /**
     * This method combines this [TokenMap] with the other [map]
     *
     * @param map The map to add
     */
    fun addAll(map: TokenMap) {
        for ((token, amt) in map) {
            add(token, amt)
        }
    }

    /**
     * This method takes the difference between this [TokenMap] and the other [map]
     *
     * @param map The map to take the difference with
     */
    fun removeAll(map: TokenMap) {
        for ((token, amt) in map) {
            remove(token, amt)
        }
    }

    /**
     * This method implements the indexing []-operator to get tokens
     *
     * @param token The token to retrieve
     * @return The number of tokens of this kind
     */
    override operator fun get(token: Token): Int = tokens[token.ordinal]

    /**
     * This method copies this [TokenMap]
     *
     * @return A copy of this map
     */
    override fun toMutableTokenMap() = MutableTokenMap(tokens.copyOf())

    /**
     * This method implements the iteration operator to allow iterating over this [TokenMap]
     *
     * @return The iterator for this map
     */
    override operator fun iterator() = TokenMapIterator(this)


    /**
     * This operator method adds the specified token
     *
     * @param other The token to add
     */
    operator fun plusAssign(other: Token) {
        add(other)
    }

    /**
     * This operator method allows combining this map with another map
     *
     * @param other The other map to add to this
     */
    operator fun plusAssign(other: TokenMap) {
        addAll(other)
    }

    /**
     * This method removes the token from this map
     *
     * @param other The token to remove from this map
     */
    operator fun minusAssign(other: Token) {
        remove(other)
    }

    /**
     * This operator method removes the token from this map
     *
     * @param other The token to remove from this map
     */
    operator fun minusAssign(other: TokenMap) {
        removeAll(other)
    }

    /**
     * This operator method implements setting a token to a specific amount
     */
    operator fun set(token: Token, amount: Int) {
        tokens[token.ordinal] = amount
    }

    /**
     * This method provides the equals functionality
     *
     * @param other The value to compare with
     * @return Whether the objects are equal
     */
    override fun equals(other: Any?): Boolean {
        return other is MutableTokenMap && tokens.contentEquals(other.tokens)
    }

    /**
     * This method provides the hashcode functionality
     *
     * @return The hash code int
     */
    override fun hashCode(): Int {
        var result = 0
        for (token in tokens) {
            result = 31 * result + token
        }
        return result
    }

    /**
     * This function implements string for this map
     *
     * @return The string representation of this object
     */
    override fun toString(): String {
        val items = joinToString { (token, amt) -> "$token: $amt" }
        return "{ $items }"
    }
}

/**
 * This abstract class represents an immutable token map, which stores tokens and their amount.
 * It is a faster and more ergonomic replacement for [HashMap<Token, Int>].
 *
 * For a mutable version, look [MutableTokenMap]
 */
@Serializable
sealed class TokenMap : Sequence<Pair<Token, Int>> {
    /**
     * This function allows the token map to get indexed by tokens
     *
     * @param token The token kind to retrieve
     * @return The number of tokens of this kind
     */
    abstract operator fun get(token: Token): Int

    /**
     * Converts this immutable token map to a mutable version.
     * This method performs a copy.
     *
     * @return A equivalent mutable copy of this immutable map
     */
    abstract fun toMutableTokenMap(): MutableTokenMap
}

/**
 * This simple function converts a map to a [TokenMap]
 *
 * @return The equivalent [TokenMap]
 */
fun Map<Token, Int>.toTokenMap(): TokenMap {
    val map = MutableTokenMap()
    for ((token, amt) in this) {
        map[token] = amt
    }
    return map
}

/**
 * This class implements iterator functionality for [TokenMap]s
 *
 * @param tokenMap The tokenMap to iterate
 * @param index The current index of this iterator
 */
class TokenMapIterator(private val tokenMap: MutableTokenMap, var index: Int = 0) : Iterator<Pair<Token, Int>> {
    /**
     * This function returns whether this iterator has a next element
     *
     * @return true or false
     */
    override fun hasNext() = index < tokenMap.tokens.size

    /**
     * This function returns the next element of this iterator
     *
     * @return A pair of token to its amount
     */
    override fun next(): Pair<Token, Int> {
        if(!hasNext()) {
            throw NoSuchElementException("There is no next pair of token and amount")
        }
        val value = Token.values()[index] to tokenMap.tokens[index]
        index += 1
        return value
    }
}