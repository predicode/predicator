package org.predicode.predicator.predicates

import reactor.core.publisher.Flux


@Suppress("NOTHING_TO_INLINE")
inline infix fun Predicate.and(other: Predicate) = and(other)

@Suppress("NOTHING_TO_INLINE")
inline infix fun Predicate.or(other: Predicate) = or(other)

@Suppress("NOTHING_TO_INLINE")
inline operator fun Predicate.not() = negate()

/**
 * Resolves this predicate.
 *
 * Resolution may involve term [expansion][org.predicode.predicator.terms.Term.expand] and applying other resolution
 * rules.
 *
 * @param resolver predicate resolver to resolve against.
 *
 * @return a [flux][Flux] emitting resolved mappings, if any.
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun Predicate.invoke(resolver: Predicate.Resolver) = resolve(resolver)
