@file:JvmMultifileClass
@file:JvmName("Predicates")
package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * Resolvable predicate.
 *
 * When predicate resolution rule [condition][Rule.condition] matches, the [known mappings][Knowns] are applied
 * to matching rule's [predicate][Rule.predicate] in order to resolve it.
 */
@FunctionalInterface
interface Predicate {

    /**
     * Resolves this predicate.
     *
     * @param resolver predicate resolver to resolve against.
     *
     * @return a [flux][Flux] emitting resolved mappings, if any.
     */
    fun resolve(resolver: PredicateResolver): Flux<Knowns>

    infix fun and(other: Predicate): Predicate = And(this, other)

    infix fun or(other: Predicate): Predicate = Or(this, other)

    operator fun not(): Predicate = Not(this)

    private data class And(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                first.resolve(resolver)
                        .flatMap { resolved -> second.resolve(resolver.withKnowns(resolved)) }

        override fun toString() = "$first, $second"

    }

    private data class Or(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                Flux.merge(first.resolve(resolver), second.resolve(resolver))

        override fun toString() = "$first; $second"

    }

    private data class Not(val negated: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                negated.resolve(resolver)
                        .next()
                        .map { true }
                        .defaultIfEmpty(false)
                        .filter { !it }
                        .map { resolver.knowns }
                        .flux()

        override fun toString() = "\\+ $negated"

    }

}
