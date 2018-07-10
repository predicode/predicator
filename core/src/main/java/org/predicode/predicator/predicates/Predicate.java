package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import org.predicode.predicator.terms.PlainTerm;
import org.predicode.predicator.terms.Term;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

import static java.util.Collections.unmodifiableList;


/**
 * Resolvable predicate.
 *
 * <p>When predicate resolution rule {@link Rule#getCondition() condition} matches, the {@link Knowns known mappings}
 * are applied to matching rule's {@link Rule#getPredicate() predicate} in order to resolve it.</p>
 */
@FunctionalInterface
@Immutable
public interface Predicate {

    /**
     * Predicate always resolved without modifying the original resolution.
     *
     * <p>This is used as the only predicate of the {@link Rule.Pattern#fact() fact}.</p>
     */
    Predicate TRUE = True.INSTANCE;

    /**
     * Returns predicate that is never resolved.
     */
    Predicate FALSE = False.INSTANCE;

    /**
     * Returns empty predicate call.
     *
     * @return empty predicate call instance.
     */
    @Nonnull
    static Call emptyCall() {
        return EmptyCall.INSTANCE;
    }

    /**
     * Creates a predicate call containing only the given terms.
     *
     * @param terms prefix terms.
     *
     * @return new predicate call.
     */
    @Nonnull
    static Call call(@Nonnull List<? extends PlainTerm> terms) {
        if (terms.isEmpty()) {
            return emptyCall();
        }
        return new FinitePrefix(terms, EmptyCall.INSTANCE);
    }

    /**
     * Creates an infinite predicate call.
     *
     * @param buildPrefix a function that extracts a prefix of the given {@code length} out of the constructed call.
     *
     * @return {@link Call#isFinite() infinite} predicate call.
     */
    @Nonnull
    static Call infiniteCall(@Nonnull IntFunction<Optional<Prefix>> buildPrefix) {
        return new InfiniteCall(buildPrefix);
    }

    /**
     * Creates a predicate prefix with empty suffix.
     *
     * @param terms prefix terms.
     *
     * @return new predicate prefix.
     */
    @Nonnull
    static Prefix prefix(@Nonnull List<? extends PlainTerm> terms) {
        return prefix(terms, emptyCall());
    }

    /**
     * Creates a predicate prefix.
     *
     * @param terms prefix terms.
     * @param suffix a suffix containing all other call terms.
     *
     * @return new predicate prefix.
     */
    @Nonnull
    static Prefix prefix(@Nonnull List<? extends PlainTerm> terms, @Nonnull Call suffix) {

        final FiniteCall finiteSuffix = suffix.toFinite();

        if (finiteSuffix != null) {
            return new FinitePrefix(terms, finiteSuffix);
        }

        return new InfinitePrefix(terms, suffix);
    }

    /**
     * Resolves this predicate.
     *
     * <p>Resolution may involve term {@link Term#expand(Resolver) expansion} and applying other resolution
     * rules.</p>
     *
     * @param resolver predicate resolver to resolve against.
     *
     * @return a {@link Flux flux} emitting resolved mappings, if any.
     */
    @Nonnull
    Flux<Knowns> resolve(@Nonnull Resolver resolver);

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

    /**
     * A call for predicate.
     *
     * <p>It is used to find matching {@link Rule predicate resolution rule} and apply it in order to perform
     * this call resolution.</p>
     *
     * <p>The call is a sequence of {@link PlainTerm plain terms}. Possibly infinite. In the latter case it matches
     * prefix rules only.</p>
     *
     * <p>The call consists of plain terms. However, they are not immediately available. To request the terms one has
     * to request a {@link #prefix(int) prefix}. The latter contains the terms.</p>
     */
    @Immutable
    abstract class Call implements Predicate, Qualified<Call> {

        Call() {
        }

        /**
         * Predicate call qualifiers.
         *
         * @return collection of qualifiers.
         */
        @Override
        @Nonnull
        public abstract Qualifiers getQualifiers();

        @Nonnull
        @Override
        public Call qualify(@Nonnull UnaryOperator<Qualifiers> updateQualifiers) {

            final Qualifiers old = getQualifiers();
            final Qualifiers updated = updateQualifiers.apply(old);

            if (old == updated) {
                return this;
            }

            return updateQualifiers(updated);
        }

        /**
         * Extracts a prefix of the given {@code length} out of this call.
         *
         * @param length the length of the prefix.
         *
         * @return an optional resolved to predicate {@link Prefix prefix}, or empty optional if the call does not
         * contain enough terms.
         */
        @Nonnull
        public final Optional<Prefix> prefix(int length) {
            if (length < 0) {
                throw new IllegalArgumentException("Prefix length can not be negative: " + length);
            }
            return buildPrefix(length);
        }

        /**
         * The number of terms this call contains.
         *
         * @return either a number of terms, or negative value to indicate that the number of terms unknown.
         * E.g. when the call contains an infinite number of terms.
         */
        public abstract int length();

        /**
         * Whether this call is empty.
         *
         * @return {@code true} if {@link #length() call size} is zero, or {@code false} otherwise.
         */
        public final boolean isEmpty() {
            return length() == 0;
        }

        /**
         * Whether this call contains a finite (and known) number of terms.
         *
         * @return {@code true} if {@link #length() call size} is non-negative value, or {@code false} otherwise.
         */
        public final boolean isFinite() {
            return toFinite() != null;
        }

        @Nonnull
        @Override
        public final Flux<Knowns> resolve(@Nonnull Resolver resolver) {
            return resolver.matchingRules(this)
                    .flatMap(match -> match.getRule()
                            .getPredicate()
                            .resolve(new CustomResolver(
                                    match.getKnowns(),
                                    (call, knowns) -> resolver.matchingRules(
                                            knowns.attr(Qualifiers.class)
                                                    .map(qualifiers -> call.qualify(old -> old.fulfill(qualifiers)))
                                                    .orElse(call)))));
        }

        @Nullable
        abstract FiniteCall toFinite();

        @Nonnull
        abstract Optional<Prefix> buildPrefix(int length);

        /**
         * Replaces qualifiers of this predicate call.
         *
         * @param qualifiers new qualifiers.
         *
         * @return new predicate call with the given qualifiers collection.
         */
        @Nonnull
        abstract Call updateQualifiers(@Nonnull Qualifiers qualifiers);

    }

    /**
     * Predicate call prefix.
     *
     * <p>It contains a list of terms in this prefix, and the rest of terms in the predicate call after the prefix.</p>
     */
    @Immutable
    abstract class Prefix extends Call {

        @Nonnull
        private final List<? extends PlainTerm> terms;

        @Nonnull
        private final Call rest;

        Prefix(
                @Nonnull List<? extends PlainTerm> terms,
                @Nonnull Call rest) {
            this.terms = unmodifiableList(terms);
            this.rest = rest;
        }

        /**
         * Predicate prefix terms.
         *
         * @return readonly list of terms.
         */
        @Nonnull
        public final List<? extends PlainTerm> getTerms() {
            return this.terms;
        }

        /**
         * The rest of the predicate call terms after the {@link #getTerms() prefix ones}.
         *
         * @return the rest of the terms represented by predicate call.
         */
        @Nonnull
        public final Call getRest() {
            return this.rest;
        }

        @Override
        @Nonnull
        public final Qualifiers getQualifiers() {
            return getRest().getQualifiers();
        }

        @Nonnull
        @Override
        public final Prefix qualify(@Nonnull Qualifier... qualifiers) {
            return (Prefix) super.qualify(qualifiers);
        }

        @Nonnull
        @Override
        public final Prefix qualify(@Nonnull Qualifiers qualifiers) {
            return (Prefix) super.qualify(qualifiers);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Prefix prefix = (Prefix) o;

            if (!this.terms.equals(prefix.terms)) {
                return false;
            }

            return this.rest.equals(prefix.rest);
        }

        @Override
        public int hashCode() {

            int result = super.hashCode();

            result = 31 * result + this.terms.hashCode();
            result = 31 * result + this.rest.hashCode();

            return result;
        }

        @Nonnull
        @Override
        abstract Prefix updateQualifiers(@Nonnull Qualifiers qualifiers);

    }

    /**
     * A resolver to resolve predicates against.
     *
     * <p>Instances of this class expected to be immutable.</p>
     */
    interface Resolver {

        /**
         * Known variable mappings and resolutions.
         */
        @Nonnull
        Knowns getKnowns();

        /**
         * Selects resolution rules the given predicate call matches.
         *
         * @param call predicate call.
         *
         * @return a {@link Flux} of {@link Rule.Match rule matches}.
         */
        @Nonnull
        Flux<Rule.Match> matchingRules(@Nonnull Predicate.Call call);

        /**
         * Constructs new predicate resolver based on this one with the given variable mappings and resolutions.
         *
         * @param knowns new variable mappings an resolutions.
         */
        default Resolver withKnowns(@Nonnull final Knowns knowns) {
            return new CustomResolver(knowns, (call, kns) -> matchingRules(call));
        }

        /**
         * Constructs new predicate resolver based on this one with the given predicate resolution rule selector.
         *
         * @param selector new predicate resolution rule selector.
         */
        default Resolver withSelector(@Nonnull Rule.Selector selector) {
            return new CustomResolver(getKnowns(), selector);
        }

    }

}
