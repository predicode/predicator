package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.function.Function

/**
 * Resolvable predicate.
 *
 * When predicate resolution rule [condition][Rule.condition] matches, the [known mappings][Knowns] are applied
 * to matching rule's [predicate][Rule.predicate] in order to resolve it.
 */
@FunctionalInterface
interface Predicate : Function<Knowns, Flux<Knowns>> {

    @JvmDefault
    override fun apply(knowns: Knowns) = resolve(knowns)

    /**
     * Resolves this predicate.
     *
     * @param knowns mappings known before resolution attempt.
     *
     * @return a [flux][Flux] emitting resolved mappings, if any.
     */
    fun resolve(knowns: Knowns): Flux<Knowns>

    infix fun and(other: Predicate): Predicate = And(this, other)

    infix fun or(other: Predicate): Predicate = Or(this, other)

    operator fun not(): Predicate = Not(this)

    private data class And(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(knowns: Knowns): Flux<Knowns> =
                first.resolve(knowns).flatMap { resolved -> second.resolve(resolved) }

        override fun toString() = "$first, $second"

    }

    private data class Or(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(knowns: Knowns): Flux<Knowns> =
                Flux.merge(first.resolve(knowns), second.resolve(knowns))

        override fun toString() = "$first; $second"

    }

    private data class Not(val negated: Predicate) : Predicate {

        override fun resolve(knowns: Knowns): Flux<Knowns> =
                negated.resolve(knowns)
                        .next()
                        .map { true }
                        .defaultIfEmpty(false)
                        .filter { !it }
                        .map { knowns }
                        .flux()

        override fun toString() = "\\+ $negated"

    }

}
