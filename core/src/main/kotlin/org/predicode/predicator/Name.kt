package org.predicode.predicator

/**
 * Name representation.
 *
 * Names are case insensitive and consists of [name parts][NamePart], that could be either words or digits.
 */
class Name(val parts: List<NamePart>) {

    companion object {
        operator fun invoke(vararg parts: NamePart) = Name(listOf(*parts))
        operator fun invoke(vararg parts: String) = Name(parts.map { NamePart(it) })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Name

        return parts == other.parts
    }

    override fun hashCode() = parts.hashCode()

    override fun toString() = parts.joinToString(" ")

}

/**
 * A part of the name.
 *
 * This could be either [Word] or [Digits]
 */
sealed class NamePart {

    /**
     * Creates new name part auto-detecting its type.
     */
    companion object {
        operator fun invoke(text: String): NamePart = if (text.first() in '0'..'9') Digits(text) else Word(text)
    }

    abstract val text: String

    abstract val word: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamePart

        return word == other.word
    }

    override fun hashCode() = word.hashCode()

    override fun toString() = text

}

/**
 * Word representation.
 *
 * Words are case insensitive and should contain letters only.
 *
 * @property text original word text.
 * @property word normalized (lower-case) word text.
 */
class Word(override val text: String): NamePart() {

    companion object {
        val REGEX = Regex("^\\p{L}+$")
    }

    init {
        assert(text.isNotEmpty()) { "Word should not be empty" }
        assert(text.matches(REGEX)) { "Word should contain letters only" }
    }

    override val word = text.toLowerCase()

}

/**
 * Decimal digits representation.
 *
 * @property text original word text.
 * @property word normalized (lower-case) word text.
 */
class Digits(override val text: String): NamePart() {

    companion object {
        val REGEX = Regex("^\\d+$")
    }

    init {
        assert(text.isNotEmpty()) { "Digits should not be empty" }
        assert(text.matches(REGEX)) { "Digits expected" }
    }

    override val word
        get() = text

}
