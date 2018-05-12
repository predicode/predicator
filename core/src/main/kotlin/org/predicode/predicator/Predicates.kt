@file:JvmName("Predicates")
package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.function.Function

/**
 * A predicate always resolved to true.
 *
 * This is used as the only predicate of the [fact][RulePattern.fact].
 */
fun alwaysTrue(): Predicate = True

private object True : Predicate {

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> = Flux.just(resolver.knowns)

    override fun toString() = "."

}

fun resolvingPredicate(_resolve: Function<PredicateResolver, Flux<Knowns>>): Predicate = object : Predicate {
    override fun resolve(resolver: PredicateResolver) = _resolve.apply(resolver)
}

fun simplePredicate(vararg terms: SimpleTerm): Predicate =
        RulePattern(*terms).let { pattern ->
            object : Predicate {
                override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                        resolver.ruleSelector.matchingRules(pattern)
                                .flatMap { (rule, knowns) ->
                                    rule.predicate.resolve(resolver.withKnowns(knowns))
                                }
            }
        }
