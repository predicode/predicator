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

    /**
     * Expands this term replacing it with plain one.
     *
     * Expansion happens e.g. when [resolving a phrase][Phrase.resolve].
     *
     * Plain terms typically expand to themselves, except for [variables][Variable], that are expanded to their
     * [mappings][Knowns.mapping].
     *
     * @param resolver predicate resolver instance to resolve/expand against.
     *
     * @return this term expansion.
     */
    abstract fun expand(resolver: PredicateResolver): Expansion

    /**
     * Returns a string representation of this term for inclusion into phrase string representation.
     */
    open fun toPhraseString() = toString()

    /**
     * Term [expansion][expand] result.
     *
     * @property expanded a plain term the original term is expanded to.
     * @property updatePredicate an operator to apply to original predicate.
     *
     * This may be used e.g. to recursively resolve additional predicates. E.g. when expanding compound
     * [phrase][Phrase].
     */
    data class Expansion(
            val expanded: PlainTerm,
            val updatePredicate: UnaryOperator<Predicate> = UnaryOperator.identity())

}

/**
 * A plain, non-compound term.
 *
 * [Rule patterns][RulePattern] may contain plain terms only.
 */
sealed class PlainTerm : Term() {

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
    abstract fun match(term: PlainTerm, knowns: Knowns): Knowns?

}

/**
 * A term the local [variable][Variable] may be mapped to.
 */
sealed class MappedTerm : PlainTerm()

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
abstract class Keyword : PlainTerm() {

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? =
            knowns.takeIf { term == this } // Keywords match only themselves

    final override fun expand(resolver: PredicateResolver) = Expansion(this)

}

/**
 * Atom term.
 *
 * Atoms match only themselves and can be mapped to variables.
 */
abstract class Atom : ResolvedTerm() {

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? = when (term) {
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

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? = when (term) {
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

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? = when (term) {
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
 * [plain term][PlainTerm] prior to being matched.
 *
 * This is also a predicate that [expands][Term.expand] all of its predicates, and then corresponding
 * [resolution rules][Rule] are searched and applied.
 *
 * @param terms terms this phrase consists of.
 */
class Phrase(private vararg val terms: Term) : Term(), Iterable<Term>, Predicate {

    override fun toString() = terms.joinToString(" ") { it.toPhraseString() }

    override fun toPhraseString() = "($this)"

    override fun expand(resolver: PredicateResolver): Expansion {
        TODO("Phrase expansion is not implemented")
    }

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> {
        return terms
                .fold(PhraseResolution(resolver, terms.size)) { resolution, term ->
                    try {
                        resolution.expandTerm(term)
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
        private val terms: Array<PlainTerm?> = arrayOfNulls(size)
        private var index = 0

        fun expandTerm(term: Term) = also {

            val expansion: Expansion = term.expand(resolver)

            terms[index++] = expansion.expanded

            predicate = expansion.updatePredicate.apply(predicate)
        }

        @Suppress("UNCHECKED_CAST")
        fun resolve(): Flux<Knowns> =
                (predicate and plainPredicate()).resolve(resolver)

        fun plainPredicate(): Predicate =
                RulePattern(*expandedTerms()).let { pattern ->
                    object : Predicate {
                        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                                resolver.ruleSelector.matchingRules(pattern)
                                        .flatMap { (rule, knowns) ->
                                            rule.predicate.resolve(resolver.withKnowns(knowns))
                                        }
                    }
                }

        @Suppress("UNCHECKED_CAST")
        fun expandedTerms(): Array<out PlainTerm> =
                terms as Array<out PlainTerm>

    }

}

