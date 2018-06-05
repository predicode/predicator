package org.predicode.predicator

import reactor.core.publisher.Flux

class TestPredicateResolver(override val knowns: Knowns) : PredicateResolver, RuleSelector {

    override fun matchingRules(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match> = this(pattern, knowns)

    override fun invoke(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match> = Flux.empty()

}
