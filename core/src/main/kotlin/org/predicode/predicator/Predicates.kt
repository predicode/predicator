package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * Returns predicate always resolved without modifying the original resolution.
 *
 * This is used as the only predicate of the [fact][RulePattern.fact].
 */
object True : Predicate {

    override fun invoke(resolver: PredicateResolver): Flux<Knowns> = Flux.just(resolver.knowns)

    override fun and(other: Predicate) = other

    override fun or(other: Predicate) = this

    override fun not() = False

    override fun toString() = "."

}

/**
 * Returns predicate that is never resolved.
 */
object False : Predicate {

    override fun invoke(resolver: PredicateResolver): Flux<Knowns> = Flux.empty()

    override fun and(other: Predicate) = this

    override fun or(other: Predicate) = other

    override fun not() = True

    override fun toString() = "\\+."

}
