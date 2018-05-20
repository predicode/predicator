package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.*
import java.util.function.Function

/**
 * Resolution rule match pattern.
 */
class RulePattern(private vararg val terms: PlainTerm) : List<PlainTerm> by terms.asList() {

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
     * Creates a rule resolved by [rule application][applyRules].
     *
     * @param terms terms the rule search pattern consists of.
     */
    fun resolveBy(vararg terms: PlainTerm) = rule(RulePattern(*terms).applyRules())

    /**
     * Creates a rule resolved by [phrase predicate][Phrase.predicate] consisting of the given terms.
     *
     * @param terms terms the phrase consists of.
     */
    fun resolveBy(vararg terms: Term) = rule(Phrase(*terms).predicate())

    /**
     * Creates predicate that searches for the rules matching this pattern and applies them.
     */
    fun applyRules() = object : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                resolver.ruleSelector.ruleMatches(this@RulePattern, resolver.knowns)
                        .flatMap { (rule, knowns) ->
                            rule.predicate.resolve(resolver.withKnowns(knowns))
                        }

        override fun toString() = this@RulePattern.toString()

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RulePattern

        return Arrays.equals(terms, other.terms)

    }

    override fun hashCode() = Arrays.hashCode(terms)

    override fun toString() = terms.joinToString(" ") { it.toPhraseString() }

}
