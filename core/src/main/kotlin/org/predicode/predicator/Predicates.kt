@file:JvmName("Predicates")
package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.function.Function

/**
 * Returns predicate always resolved without modifying the original resolution.
 *
 * This is used as the only predicate of the [fact][RulePattern.fact].
 */
fun alwaysTrue(): Predicate = True

private object True : Predicate {

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> = Flux.just(resolver.knowns)

    override fun and(other: Predicate) = other

    override fun or(other: Predicate) = this

    override fun not() = False

    override fun toString() = "."

}

/**
 * Returns predicate that is never resolved.
 */
fun alwaysFalse(): Predicate = False

private object False : Predicate {

    override fun resolve(resolver: PredicateResolver): Flux<Knowns> = Flux.empty()

    override fun and(other: Predicate) = this

    override fun or(other: Predicate) = other

    override fun not() = True

    override fun toString() = "\\+."

}

/**
 * Creates predicate resolved by the given function.
 *
 * @param resolve predicate resolution function.
 */
fun resolvingPredicate(resolve: Function<PredicateResolver, Flux<Knowns>>): Predicate = object : Predicate {
    override fun resolve(resolver: PredicateResolver) = resolve.apply(resolver)
}
