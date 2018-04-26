package org.predicode.predicator

import reactor.core.publisher.Mono

/**
 * A set of predicate resolution rules.
 */
interface RuleSet {

    /**
     * Searches for the matching predicate resolution rule.
     *
     * @param pattern resolution rule pattern to match agains.
     *
     * @return a [Mono] resolving to matching rules (either simple or compound one), or empty mono if nothing matches.
     */
    fun findRule(pattern: RulePattern): Mono<Rule>

}
