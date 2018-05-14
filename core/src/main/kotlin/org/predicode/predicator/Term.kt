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
 * - [phrase][Phrase]
 */
sealed class Term {

    abstract fun expand(resolver: PredicateResolver): Expansion

    /**
     * Returns a string representation of this term for inclusion into phrase string representation.
     */
    open fun toPhraseString() = toString()

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
 * A term the local [variable][Variable] may be mapped to.
 */
sealed class MappedTerm : SimpleTerm()

/**
 * A term the query [variable][Variable] may resolve to.
 */
sealed class ResolvedTerm : MappedTerm() {

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

    override fun toPhraseString() = "($this)"

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

    override fun toPhraseString() = "[$this]"

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
 *
 * Variable can be either local to rule, or global, i.e. present in original query. The former should be
 * [mapped][Knowns.map] to their values prior to [predicate resolution][Predicate.resolve].
 * All of the latter should be specified when [constructing knowns][Knowns] and are to be [resolved][Knowns.resolve].
 */
abstract class Variable : MappedTerm() {

    final override fun match(term: SimpleTerm, knowns: Knowns): Knowns? = when (term) {
        is MappedTerm -> knowns.map(this, term)
        is Keyword -> null // Keywords are not acceptable as variable values
    }

    final override fun expand(resolver: PredicateResolver) =
            resolver.knowns
                    .mapping(this)
                    .let { Expansion(it) }

}

/**
 * A phrase consisting of other terms.
 *
 * A phrase can not be part of [rule patterns][RulePattern] and thus should be [expanded][expand] to
 * [simple term][SimpleTerm] prior to being matched.
 */
class Phrase(vararg _terms: Term) : Term(), Iterable<Term>, Predicate {

    private val terms: Array<out Term> = _terms

    override fun toString() = terms.joinToString(" ") { it.toPhraseString() }

    override fun toPhraseString() = "($this)"

    override fun expand(resolver: PredicateResolver): Expansion {
        TODO("Phrase expansion is not implemented")
    }

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> {
        return terms
                .withIndex()
                .fold(PhraseResolution(resolver, terms.size)) { resolution, (index, term) ->
                    try {
                        resolution.expand(index, term)
                    } catch (e: Exception) {
                        return Flux.error(e)
                    }
                }
                .resolve()
    }

    override fun iterator(): Iterator<Term> = this.terms.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Phrase

        return Arrays.equals(terms, other.terms)

    }

    override fun hashCode(): Int {
        return Arrays.hashCode(terms)
    }

    private class PhraseResolution(val resolver: PredicateResolver, size: Int) {

        private var predicate: Predicate = alwaysTrue()
        private val terms: Array<Term?> = arrayOfNulls(size)

        fun expand(index: Int, term: Term): PhraseResolution {

            val expansion: Expansion = term.expand(resolver)

            terms[index] = expansion.expanded

            predicate = expansion.updatePredicate.apply(predicate)

            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun resolve(): Flux<Knowns> =
                (predicate and simplePredicate(*(terms as Array<SimpleTerm>))).resolve(resolver)

    }

}
