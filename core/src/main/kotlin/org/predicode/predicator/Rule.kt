package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.function.Function

/**
 * A selector of predicate resolution rules.
 *
 * Searches for the matching predicate resolution rule.
 *
 * This is a function that takes a rule query as the only parameter and returning a [Flux] resolving to matching rules.
 */
typealias RuleSelector = Function<RulePattern, Flux<Rule>>

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

}
