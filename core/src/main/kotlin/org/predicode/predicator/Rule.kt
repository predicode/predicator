package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * A selector of predicate resolution rules.
 *
 * Searches for the matching predicate resolution rule.
 *
 * @param pattern resolution rule pattern to match agains.
 *
 * @return a [Flux] resolving to matching rules.
 */
typealias RuleSelector = (pattern: RulePattern) -> Flux<Rule>

/**
 * Predicate resolution rule.
 */
interface Rule {

}
