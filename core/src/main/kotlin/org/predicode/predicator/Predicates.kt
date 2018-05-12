@file:JvmName("Predicates")
package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * A predicate always resolved to true.
 */
@JvmField
val TRUE: Predicate = True

private object True : Predicate {

    override fun resolve(knowns: Knowns): Flux<Knowns> = Flux.just(knowns)

    override fun toString() = "true"

}
