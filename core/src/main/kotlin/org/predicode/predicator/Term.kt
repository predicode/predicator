package org.predicode.predicator

import java.util.function.UnaryOperator

/**
 * Basic term.
 *
 * Can be one of:
 * - [keyword][Keyword]
 * - [atom][Atom],
 * - [arbitrary value][Value],
 * - [variable][Variable], or
 * - any [compound term][CompoundTerm], such as [phrase][Phrase]
 */
sealed class Term {

    /**
     * Expands this term replacing it with plain one.
     *
     * Expansion happens e.g. when resolving a [phrase predicate][Phrase.predicate].
     *
     * Plain terms typically expand to themselves, except for [variables][Variable], that are expanded to their
     * [mappings][Knowns.mapping].
     *
     * @param resolver predicate resolver instance to resolve/expand against.
     *
     * @return this term expansion.
     */
    abstract fun expand(resolver: PredicateResolver): Expansion?

    /**
     * Returns a string representation of this term for inclusion into phrase string representation.
     */
    open fun toPhraseString() = toString()

    /**
     * Term [expansion][expand] result.
     *
     * @property expanded a plain term the original term is expanded to.
     * @property knowns updated knowns.
     * @property updatePredicate an operator to apply to original predicate.
     *
     * This may be used e.g. to recursively resolve additional predicates. E.g. when expanding compound
     * [phrase][Phrase].
     */
    data class Expansion(
            val expanded: PlainTerm,
            val knowns: Knowns,
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

    final override fun expand(resolver: PredicateResolver) =
            Expansion(this, resolver.knowns)

}

/**
 * Keyword term.
 *
 * Keywords match only themselves. They can not be mapped to variables.
 */
abstract class Keyword : PlainTerm() {

    /**
     * Keyword name.
     *
     * This is used generally for representation only.
     */
    abstract val name: String

    final override fun match(term: PlainTerm, knowns: Knowns) =
            knowns.takeIf { term == this } // Keywords match only themselves

    final override fun expand(resolver: PredicateResolver) =
            Expansion(this, resolver.knowns)

    override fun toString() = "'$name'"

    override fun toPhraseString() = name

}

/**
 * Atom term.
 *
 * Atoms match only themselves and can be mapped to variables.
 */
abstract class Atom : ResolvedTerm() {

    /**
     * Atom name.
     *
     * This is used generally for representation only.
     */
    abstract val name: String

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? = when (term) {
        is Keyword -> null // Keywords match only themselves
        is Atom -> knowns.takeIf { term == this }
        is Value -> null // Words never match values
        is Variable -> knowns.resolve(term, this)
    }

    override fun toString() = name

    override fun toPhraseString() = "($name)"

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

    /**
     * Variable name.
     *
     * This is used generally for representation only.
     */
    abstract val name: String

    final override fun match(term: PlainTerm, knowns: Knowns): Knowns? = when (term) {
        is MappedTerm -> knowns.map(this, term)
        is Keyword -> null // Keywords are not acceptable as variable values
    }

    final override fun expand(resolver: PredicateResolver) =
            resolver.knowns.mapping(this) { mapping, knowns ->
                Expansion(mapping, knowns)
            }

    /**
     * Builds rule pattern corresponding to definition of some expression.
     *
     * @param terms expression terms.
     *
     * @return rule pattern matching expression definition.
     */
    fun definitionOf(vararg terms: PlainTerm) =
            RulePattern(*(arrayOf(this, definitionKeyword()) + terms))

    override fun toString(): String = "_${name}_"

}

/**
 * A compound term that may contain other terms.
 *
 * A compound terms can not be part of [rule patterns][RulePattern] and thus should be [expanded][expand] to
 * [plain term][PlainTerm] prior to being matched.
 */
abstract class CompoundTerm : Term()

