package service

/**
 * This helper class can be used to send data to a generator
 */
class Yielder<T> {
    /**
     * The current sequence that is being iterated
     */
    lateinit var sequence: Iterator<T>

    /**
     * The current argument to that sequence
     */
    var argument: Any? = null


    /**
     * Continues the sequence with [value]
     *
     * @param value The value to continue with
     * @return The value returned by the sequence
     */
    fun continueWith(value: Any?): T? {
        argument = value
        if (hasNext()){
            return sequence.next()
        }
        return null
    }

    /**
     * @return Whether the sequence has a next element
     */
    fun hasNext() = try {
        sequence.hasNext()
    } catch (e: IllegalStateException) {
        if(e.localizedMessage == "Detekted") {
            println("We were detekted")
        }
        false
    }
}

/**
 * This builder function takes a code block and converts it into a generator that can accept a parameter
 *
 * @param block The code block
 * @return The wrapping [Yielder] class
 */
fun <T> yielder(block: suspend SequenceScope<T>.(Yielder<T>) -> Unit): Yielder<T> {
    val yielder = Yielder<T>()
    yielder.sequence = iterator { block(yielder) }
    return yielder
}