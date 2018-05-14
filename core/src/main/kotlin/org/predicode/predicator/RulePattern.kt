package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.*
import java.util.function.Function

/**
 * Resolution rule match pattern.
 */
class RulePattern(vararg _terms: PlainTerm) : Iterable<PlainTerm> {

    private val terms: Array<out PlainTerm> = _terms

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

        var result = knowns.update()
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
     * Creates a fact with this pattern as its [condition][Rule.condition].
     */
    fun fact() = rule(alwaysTrue())

    /**
     * Creates a rule resolved by the given predicate resolution function.
     *
     * @param resolve predicate resolution function.
     */
    fun resolveBy(resolve: Function<PredicateResolver, Flux<Knowns>>) = rule(resolvingPredicate(resolve))

    /**
     * Creates a rule resolved by [phrase][Phrase] consisting of the given terms.
     *
     * @param terms terms the phrase consists of.
     */
    fun resolveBy(vararg terms: Term) = rule(Phrase(*terms))

    override fun iterator() = terms.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RulePattern

        return Arrays.equals(terms, other.terms)

    }

    override fun hashCode() = Arrays.hashCode(terms)

    override fun toString() = terms.joinToString(" ") { it.toPhraseString() }

}
