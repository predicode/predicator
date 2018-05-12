package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * Predicate resolution rule.
 *
 * The [known mappings][Knowns] returned by [condition] are passed to [predicate]. It should be compatible.
 *
 * @property condition a condition this rule matches.
 * @property predicate predicated this rule resolves to when matched.
 */
data class Rule(val condition: RulePattern, val predicate: Predicate) {

    override fun toString() = "$condition :- $predicate"

    /**
     * A selector of predicate resolution rules.
     */
    @FunctionalInterface
    interface Selector {

        /**
         * Searches for the matching predicate resolution rules.
         *
         * @param pattern rule search pattern.
         *
         * @return a [Flux] of [rule matches][Match].
         */
        fun matchingRules(pattern: RulePattern): Flux<Match>

    }

    data class Match(val rule: Rule, val knowns: Knowns);

}
