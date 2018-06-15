package org.predicode.predicator

import reactor.core.publisher.Flux

class TestPredicateResolver(private val knowns: Knowns) : PredicateResolver, Rule.Selector {

    override fun getKnowns() = knowns

    override fun matchingRules(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match> = Flux.empty()

}
