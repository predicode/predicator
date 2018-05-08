package org.predicode.predicator

/**
 * Basic term.
 *
 * Can be one of:
 * - [keyword][Keyword]
 * - [atom][Atom],
 * - [arbitrary value][Value], or
 * - [variable][Variable],
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
sealed class SimpleTerm : Term() {

    /**
     * Attempts to match against another term.
     *
     * This method is called for the terms of the [rule condition][Rule.condition] with corresponding query term
     * as argument.
     *
     * @param term a term to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or `null` otherwise.
     */
    abstract fun match(term: SimpleTerm, knowns: Knowns): Knowns?

    // TODO Implement inherent value matching
    protected fun <V> valueMatch(pattern: V, value: V?) = pattern == value

}

/**
 * A term the [variable][Variable] may resolve to.
 */
sealed class ResolvedTerm : SimpleTerm()

/**
 * Keyword term.
 */
open class Keyword(val name: String) : SimpleTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.takeIf { term == this } // Keywords match only themselves

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Keyword

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = "<$name>"

    override fun toChainString() = name

}

/**
 * Atom term.
 */
open class Atom(val name: String) : ResolvedTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Atom -> knowns.takeIf { term == this }
        is Value<*> -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Atom

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name

    override fun toChainString() = "($this)"

}

/**
 * Arbitrary value term.
 */
open class Value<out V>(val value: V) : ResolvedTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Value<*> -> knowns.takeIf { this == term }
        is Atom -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Value<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString() = value.toString()

    override fun toChainString() = "[$this]"

}

/**
 * Variable term.
 */
data class Variable(val name: String) : SimpleTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.map(this, term)

    override fun toString(): String = "_${name}_"

}

/**
 * A chain term consisting of other terms.
 */
data class TermChain(val terms: List<Term>) : Term() {

    /**
     * Constructs term chain by its terms.
     */
    constructor(vararg terms: Term) : this(listOf(*terms))

    override fun toString() = terms.joinToString(" ") { it.toChainString() }

    override fun toChainString() = "($this)"

}

