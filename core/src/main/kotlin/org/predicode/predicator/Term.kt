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

    companion object {

        @JvmStatic
        fun namedKeyword(name: String): Keyword = NamedKeyword(name)

        @JvmStatic
        fun namedAtom(name: String): Atom = NamedAtom(name)

        @JvmStatic
        fun simpleValue(value: Any): Value = SimpleValue(value)

        @JvmStatic
        fun namedVariable(name: String): Variable = NamedVariable(name)

    }

}

fun namedKeyword(name: String) = Term.namedKeyword(name)

fun namedAtom(name: String) = Term.namedAtom(name)

fun simpleValue(value: Any) = Term.simpleValue(value)

fun namedVariable(name: String) = Term.namedVariable(name)

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

}

/**
 * A term the [variable][Variable] may resolve to.
 */
sealed class ResolvedTerm : SimpleTerm()

/**
 * Keyword term.
 */
abstract class Keyword : SimpleTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.takeIf { term == this } // Keywords match only themselves

}

private class NamedKeyword(val name: String) : Keyword() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedKeyword

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "'$name'"

    override fun toChainString() = name

}

/**
 * Atom term.
 */
abstract class Atom : ResolvedTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Atom -> knowns.takeIf { term == this }
        is Value -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun toChainString() = "($this)"

}

private class NamedAtom (val name: String) : Atom() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedAtom

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name

}

/**
 * Arbitrary value term.
 */
abstract class Value : ResolvedTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Value -> valueMatch(term, knowns)
        is Atom -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun toChainString() = "[$this]"

    protected abstract fun valueMatch(other: Value, knowns: Knowns): Knowns?;

}

private class SimpleValue<out V>(val value: V) : Value() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleValue<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString() = value.toString()

    override fun valueMatch(other: Value, knowns: Knowns) =
            knowns.takeIf { this == other }

}

/**
 * Variable term.
 */
abstract class Variable : SimpleTerm() {

    override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.map(this, term)

}

private class NamedVariable(val name: String) : Variable() {

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
