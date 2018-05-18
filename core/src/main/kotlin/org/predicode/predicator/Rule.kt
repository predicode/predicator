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
     * A selector of matching predicate resolution rules.
     */
    @FunctionalInterface
    interface Selector {

        /**
         * Selects matching predicate resolution rules.
         *
         * @param pattern rule search pattern.
         * @param knowns known resolutions.
         *
         * @return a [Flux] of [rule matches][Match].
         */
        fun ruleMatches(pattern: RulePattern, knowns: Knowns): Flux<Match>

    }

    /**
     * Selected rule match.
     *
     * @param rule matching rule.
     * @param knowns known variable mappings and resolutions returned from [rule condition][Rule.condition]
     * [match][RulePattern.match].
     */
    data class Match(val rule: Rule, val knowns: Knowns)

}
