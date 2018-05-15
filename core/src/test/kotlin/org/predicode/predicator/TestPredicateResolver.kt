package org.predicode.predicator

import reactor.core.publisher.Flux

class TestPredicateResolver(override val knowns: Knowns) : PredicateResolver, Rule.Selector {

    override val ruleSelector: Rule.Selector = this

    override fun matchingRules(pattern: RulePattern): Flux<Rule.Match> = Flux.empty()

}