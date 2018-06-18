package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.PredicateResolver;
import org.predicode.predicator.Rule;
import org.predicode.predicator.RulePattern;
import org.predicode.predicator.annotations.SamWithReceiver;
import org.predicode.predicator.terms.Term;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


/**
 * Resolvable predicate.
 *
 * <p>When predicate resolution rule {@link Rule#getCondition() condition} matches, the {@link Knowns known mappings}
 * are applied to matching rule's {@link Rule#getPredicate() predicate} in order to resolve it.</p>
 */
@FunctionalInterface
@SamWithReceiver
public interface Predicate {

    /**
     * Predicate always resolved without modifying the original resolution.
     *
     * <p>This is used as the only predicate of the {@link RulePattern#fact() fact}.</p>
     */
    Predicate TRUE = True.INSTANCE;

    /**
     * Returns predicate that is never resolved.
     */
    Predicate FALSE = False.INSTANCE;

    /**
     * Resolves this predicate.
     *
     * <p>Resolution may involve term {@link Term#expand(PredicateResolver) expansion} and applying other resolution
     * rules.</p>
     *
     * @param resolver predicate resolver to resolve against.
     *
     * @return a {@link Flux flux} emitting resolved mappings, if any.
     */
    @Nonnull
    Flux<Knowns> resolve(@Nonnull PredicateResolver resolver);

    /**
     * Constructs predicates conjunction.
     *
     * @param other a predicate to conjunct with.
     *
     * @return predicate that is resolved by successfully resolving both predicates.
     */
    @Nonnull
    default Predicate and(@Nonnull Predicate other) {
        return new And(this, other);
    }

    /**
     * Constructs predicates disjunction.
     *
     * @param other a predicate to disjunct with.
     *
     * @return predicate that resolves to the results of the both predicates resolution.
     */
    @Nonnull
    default Predicate or(@Nonnull Predicate other){
        return new Or(this, other);
    }

    /**
     * Constructs logical negation of this predicate.
     *
     * @return predicate that is resolved successfully only when this predicate fails to resolve.
     */
    @Nonnull
    default Predicate negate() {
        return new Not(this);
    }

}
