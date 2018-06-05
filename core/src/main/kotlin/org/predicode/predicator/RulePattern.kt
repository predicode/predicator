package org.predicode.predicator

import org.predicode.predicator.grammar.printTerms
import reactor.core.publisher.Flux

/**
 * Resolution rule match pattern.
 *
 * @constructor constructs rule pattern.
 *
 * @property terms list of terms this pattern consists of.
 */
class RulePattern(val terms: List<PlainTerm>) : Predicate {

    /**
     * Constructs new rule pattern out of terms array.
     *
     * @param terms array of terms this pattern consists of.
     */
    constructor(vararg terms: PlainTerm) : this(terms.asList())

    /**
     * Attempts to match against another pattern.
     *
     * This method is called for the [rule condition][Rule.condition] with query pattern as argument.
     *
     * @param pattern a pattern to match against.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the pattern matches, or `null` otherwise.
     */
    fun match(pattern: RulePattern, knowns: Knowns): Knowns? {
        if (pattern.terms.size != terms.size) return null

        var result = knowns.startMatching()
        var index = 0

        @Suppress("UseWithIndex")
        for (term in terms) {
            result = term.match(pattern.terms[index], result) ?: return null
            ++index
        }

        return result
    }

    /**
     * Creates a resolution rule with this pattern as its [condition][Rule.condition].
     *
     * @param predicate predicate the constructed rule resolves to if this pattern matches.
     */
    fun rule(predicate: Predicate) = Rule(this, predicate)

    /**
     * Creates a resolution rule with this pattern as its [condition][Rule.condition].
     *
     * @param predicate predicate the constructed rule resolves to if this pattern matches.
     */
    fun rule(predicate: PredicateFn) = Rule(this, predicate.asPredicate())

    /**
     * Creates a fact with this pattern as its [condition][Rule.condition].
     */
    fun fact() = rule(True)

    /**
     * Creates a rule resolved by [rules application][resolve].
     *
     * @param terms terms the rule search pattern consists of.
     */
    fun resolveBy(vararg terms: PlainTerm) = rule(RulePattern(*terms))

    /**
     * Creates a rule [resolved by phrase predicate][Phrase.resolve] consisting of the given terms.
     *
     * @param terms terms the phrase consists of.
     */
    fun resolveBy(vararg terms: Term) = rule(Phrase(*terms))

    /**
     * Searches for the rules matching this pattern and applies them.
     */
    override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
            resolver.matchingRules(this@RulePattern, resolver.knowns)
                    .flatMap { (rule, knowns) ->
                        rule.predicate(resolver.withKnowns(knowns))
                    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RulePattern

        if (terms != other.terms) return false

        return true
    }

    override fun hashCode(): Int {
        return terms.hashCode()
    }

    override fun toString() = printTerms(terms)

}
