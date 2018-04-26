package org.predicode.predicator

/**
 * Basic term.
 *
 * Can be one of:
 * - [word][WordTerm],
 * - [variable][VariableTerm],
 * - [arbitrary value][ValueTerm], or
 * - [chain of terms][TermChain]
 */
sealed class Term {

    /**
     * Returns a string representation of this term for inclusion into chain string representation.
     */
    open fun toChainString() = toString()

}

/**
 * Word term.
 */
data class WordTerm(val word: Word): Term() {

    companion object {
        operator fun invoke(word: String) = WordTerm(Word(word))
    }

    override fun toString() = word.toString()

}

/**
 * Variable term.
 */
data class VariableTerm(val name: Name): Term() {

    companion object {
        operator fun invoke(vararg parts: NamePart) = VariableTerm(Name(*parts))
        operator fun invoke(vararg parts: String) = VariableTerm(Name(parts.map { NamePart(it) }))
    }

    override fun toString(): String = "_${name}_"

}

/**
 * Arbitrary value term.
 */
data class ValueTerm<out V>(val value: V): Term() {

    override fun toString() = value.toString()

    override fun toChainString() = "[$value]"

}

/**
 * A chain term consisting of other terms.
 */
data class TermChain(val terms: List<Term>): Term() {

    companion object {
        operator fun invoke(vararg terms: Term) = TermChain(listOf(*terms))
    }

    override fun toString() = terms.joinToString(" ") { it.toChainString() }

    override fun toChainString() = "($this)"

}
