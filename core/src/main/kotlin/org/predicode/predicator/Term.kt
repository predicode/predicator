package org.predicode.predicator

/**
 * Basic term.
 *
 * Can be one of:
 * - [word][WordTerm],
 * - [arbitrary value][ValueTerm], or
 * - [variable][VariableTerm],
 * - [chain of terms][TermChain]
 */
sealed class Term {

    /**
     * Returns a string representation of this term for inclusion into chain string representation.
     */
    open fun toChainString() = toString()

}

/**
 * Simple (non-compound) term.
 */
sealed class SimpleTerm: Term() {

    /**
     * Attempts to match against another term.
     *
     * This method is called for the terms of the [rule patterns][RulePattern] tested for match by
     * [rule selector][RuleSelector].
     *
     * @param term a term to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or `null` otherwise.
     */
    abstract fun match(term: SimpleTerm, knowns: Knowns): Knowns?

    // TODO Implement inherent value matching
    internal fun <V> valueMatch(pattern: V, value: V?) = pattern == value

}

sealed class ResolvedTerm: SimpleTerm()

/**
 * Word term.
 */
data class WordTerm(val word: Word): ResolvedTerm() {

    companion object {
        operator fun invoke(word: String) = WordTerm(Word(word))
    }

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is WordTerm -> if (term.word == word) knowns else null
        is ValueTerm<*> -> null // Words never match values
        is VariableTerm -> knowns.resolve(term.name, this)
    }

    override fun toString() = word.toString()

}

/**
 * Arbitrary value term.
 */
data class ValueTerm<out V>(val value: V): ResolvedTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is ValueTerm<*> -> if (valueMatch(value, term.value)) knowns else null
        is WordTerm -> null // Words never match values
        is VariableTerm -> knowns.resolve(term.name, this)
    }

    override fun toString() = value.toString()

    override fun toChainString() = "[$value]"

}

/**
 * Variable term.
 */
data class VariableTerm(val name: Name): SimpleTerm() {

    companion object {
        operator fun invoke(vararg parts: NamePart) = VariableTerm(Name(*parts))
        operator fun invoke(vararg parts: String) = VariableTerm(Name(parts.map { NamePart(it) }))
    }

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
        knowns.map(name, term)

    override fun toString(): String = "_${name}_"

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

