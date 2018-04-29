package org.predicode.predicator

/**
 * Name representation.
 *
 * Names are case insensitive and consists of [name parts][NamePart], that could be either words or digits.
 */
data class Name(val parts: List<NamePart>) {

    /**
     * Constructs name by its parts.
     */
    constructor(vararg parts: NamePart) : this(listOf(*parts))

    /**
     * Constructs name by its part texts.
     */
    constructor(vararg parts: String) : this(parts.map { NamePart(it) })

    override fun toString() = parts.joinToString(" ")

}

/**
 * A part of the name.
 *
 * This could be either [Word] or [Digits]
 */
sealed class NamePart {

    /**
     * Unmodified part text.
     */
    abstract val text: String

    /**
     * Normalized part text.
     */
    abstract val word: String

    fun component1() = this.word

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamePart

        return word == other.word
    }

    override fun hashCode() = word.hashCode()

    override fun toString() = text

    companion object {

        /**
         * Creates new name part auto-detecting its type.
         */
        operator fun invoke(text: String): NamePart =
                if (text.first() in '0'..'9') Digits(text) else Word(text)

    }

}

/**
 * Regular expression matching [word][Word].
 */
val WORD_REGEX = Regex("^\\p{L}+$")

/**
 * Word representation.
 *
 * Words are case insensitive and should contain letters only.
 *
 * @property text original word text.
 * @property word normalized (lower-case) word text.
 */
class Word(override val text: String) : NamePart() {

    init {
        assert(text.isNotEmpty()) { "Word should not be empty" }
        assert(text.matches(WORD_REGEX)) { "Word should contain letters only" }
    }

    override val word = text.toLowerCase()

}

/**
 * Regular expression matching [digits][Digits].
 */
val DIGITS_REGEX = Regex("^\\d+$")

/**
 * Decimal digits representation.
 *
 * @property text original word text.
 * @property word normalized (lower-case) word text.
 */
class Digits(override val text: String) : NamePart() {

    init {
        assert(text.isNotEmpty()) { "Digits should not be empty" }
        assert(text.matches(DIGITS_REGEX)) { "Digits expected" }
    }

    override val word
        get() = text

}
