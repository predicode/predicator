package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.*
import java.util.function.UnaryOperator

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

    abstract fun expand(resolver: PredicateResolver): Expansion

    /**
     * Returns a string representation of this term for inclusion into chain string representation.
     */
    open fun toChainString() = toString()

    data class Expansion(
            val expanded: SimpleTerm,
            val updatePredicate: UnaryOperator<Predicate> = UnaryOperator.identity())

}

/**
 * Simple (non-compound) term.
 *
 * [Rule patterns][RulePattern] may contain simple terms only.
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
sealed class ResolvedTerm : SimpleTerm() {

    final override fun expand(resolver: PredicateResolver) = Expansion(this)

}

/**
 * Keyword term.
 *
 * Keywords match only themselves. They can not be mapped to variables.
 */
abstract class Keyword : SimpleTerm() {

    final override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.takeIf { term == this } // Keywords match only themselves

    final override fun expand(resolver: PredicateResolver) = Expansion(this)

}

/**
 * Atom term.
 *
 * Atoms match only themselves and can be mapped to variables.
 */
abstract class Atom : ResolvedTerm() {

    final override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Atom -> knowns.takeIf { term == this }
        is Value -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun toChainString() = "($this)"

}

/**
 * Arbitrary value term.
 *
 * Values match only [matching][valueMatch] values and can be mapped to variables.
 */
abstract class Value : ResolvedTerm() {

    final override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Value -> valueMatch(term, knowns)
        is Atom -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun toChainString() = "[$this]"

    /**
     * Attempts to match against another value.
     *
     * This method is called from [match] one when the term to match is a value.
     *
     * @param other another value to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or `null` otherwise.
     */
    protected abstract fun valueMatch(other: Value, knowns: Knowns): Knowns?

}

/**
 * Variable term.
 */
abstract class Variable : SimpleTerm() {

    final override fun match(term: SimpleTerm, knowns: Knowns): Knowns? =
            knowns.map(this, term)

    final override fun expand(resolver: PredicateResolver) =
            resolver.knowns
                    .mapping(this)
                    .let { Expansion(it) }

}

/**
 * A chain term consisting of other terms.
 *
 * A term chain can not be part of [rule patterns][RulePattern] and thus should be [expanded][expand] to
 * [simple term][SimpleTerm] prior to being matched.
 */
class TermChain(vararg _terms: Term) : Term(), Iterable<Term>, Predicate {

    private val terms: Array<out Term> = _terms

    override fun toString() = terms.joinToString(" ") { it.toChainString() }

    override fun toChainString() = "($this)"

    override fun expand(resolver: PredicateResolver): Expansion {
        TODO("Chain expansion is not implemented")
    }

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> {
        return terms
                .fold(alwaysTrue() to mutableListOf<SimpleTerm>()) { (pred, terms), term ->

                    val expansion: Expansion = try {
                        term.expand(resolver)
                    } catch (ex: Exception) {
                        return Flux.error(ex)
                    }

                    terms.add(expansion.expanded)

                    expansion.updatePredicate.apply(pred) to terms
                }.let { (pred, terms) ->
                    pred and simplePredicate(*terms.toTypedArray())
                }.resolve(resolver)
    }

    override fun iterator(): Iterator<Term> = this.terms.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TermChain

        return Arrays.equals(terms, other.terms)

    }

    override fun hashCode(): Int {
        return Arrays.hashCode(terms)
    }

}
