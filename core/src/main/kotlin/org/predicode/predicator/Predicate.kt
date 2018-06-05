package org.predicode.predicator

import reactor.core.publisher.Flux

/**
 * Predicate function.
 */
typealias PredicateFn = (PredicateResolver) -> Flux<Knowns>

/**
 * Resolvable predicate.
 *
 * When predicate resolution rule [condition][Rule.condition] matches, the [known mappings][Knowns] are applied
 * to matching rule's [predicate][Rule.predicate] in order to resolve it.
 */
interface Predicate {

    /**
     * Resolves this predicate.
     *
     * Resolution may involve term [expansion][Term.expand] and applying other resolution rules.
     *
     * @param resolver predicate resolver to resolve against.
     *
     * @return a [flux][Flux] emitting resolved mappings, if any.
     */
    fun resolve(resolver: PredicateResolver): Flux<Knowns>

    /**
     * Constructs predicates conjunction.
     *
     * @param other a predicate to conjunct with.
     *
     * @return predicate that is resolved by successfully resolving both predicates.
     */
    @JvmDefault
    infix fun and(other: Predicate): Predicate = And(this, other)

    /**
     * Constructs predicates disjunction.
     *
     * @param other a predicate to disjunct with.
     *
     * @return predicate that resolves to the results of the both predicates resolution.
     */
    @JvmDefault
    infix fun or(other: Predicate): Predicate = Or(this, other)

    /**
     * Constructs logical negation of this predicate.
     *
     * @return predicate that is resolved successfully only when this predicate fails to resolve.
     */
    @JvmDefault
    operator fun not(): Predicate = Not(this)

    private data class And(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                first(resolver)
                        .flatMap { resolved -> second(resolver.withKnowns(resolved)) }

        override fun toString() = "$first, $second"

    }

    private data class Or(val first: Predicate, val second: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                Flux.merge(first(resolver), second(resolver))

        override fun toString() = "$first; $second"

    }

    private data class Not(val negated: Predicate) : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> =
                negated(resolver)
                        .next()
                        .map { true }
                        .defaultIfEmpty(false)
                        .filter { !it }
                        .map { resolver.knowns }
                        .flux()

        override fun toString() = "\\+ $negated"

    }

}

/**
 * Converts this predicate function to predicate.
 */
fun PredicateFn.asPredicate() = object : Predicate {
    override fun resolve(resolver: PredicateResolver): Flux<Knowns> = this@asPredicate(resolver)
}

/**
 * Resolves this predicate.
 *
 * Resolution may involve term [expansion][Term.expand] and applying other resolution rules.
 *
 * @param resolver predicate resolver to resolve against.
 *
 * @return a [flux][Flux] emitting resolved mappings, if any.
 */
operator fun Predicate.invoke(resolver: PredicateResolver) = resolve(resolver)
