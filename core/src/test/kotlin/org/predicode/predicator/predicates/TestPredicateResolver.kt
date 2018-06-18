package org.predicode.predicator.predicates

import org.predicode.predicator.Knowns
import org.predicode.predicator.Rule
import reactor.core.publisher.Flux

class TestPredicateResolver(private val knowns: Knowns) : Predicate.Resolver,
        Rule.Selector {

    override fun getKnowns() = knowns

    override fun matchingRules(call: Predicate.Call, knowns: Knowns): Flux<Rule.Match> = Flux.empty()

}
