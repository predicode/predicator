package org.predicode.predicator

import org.predicode.predicator.Rule.Match
import reactor.core.publisher.Flux

/**
 * Predicate resolver to resolve predicates against.
 *
 * An instance of this class is immutable.
 */
interface PredicateResolver {

    /**
     * Known variable mappings and resolutions.
     */
    val knowns: Knowns

    /**
     * Selects matching predicate resolution rules.
     *
     * @param pattern rule search pattern.
     * @param knowns known resolutions.
     *
     * @return a [Flux] of [rule matches][Match].
     */
    fun matchingRules(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match>

    /**
     * Constructs new predicate resolver based on this one with the given variable mappings an resolutions.
     *
     * @param knowns new variable mappings an resolutions.
     */
    @JvmDefault
    fun withKnowns(knowns: Knowns): PredicateResolver = object : PredicateResolver {
        override val knowns = knowns
        override fun matchingRules(pattern: RulePattern, knowns: Knowns) =
                this@PredicateResolver.matchingRules(pattern, knowns)
    }

}
