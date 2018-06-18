package org.predicode.predicator;

import org.predicode.predicator.annotations.SamWithReceiver;
import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.terms.PlainTerm;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Predicate resolution rule.
 *
 * <p>The {@link Knowns known mappings} returned by {@link #getCondition() condition} are passed to
 * {@link #getPredicate() predicate}. They should be compatible.</p>
 */
@Immutable
public final class Rule {

    /**
     * Creates a resolution rule pattern that matches a predicate call exactly.
     *
     * @param terms terms to match.
     *
     * @return new resolution rule pattern
     */
    @Nonnull
    public static Rule.Pattern pattern(@Nonnull List<? extends PlainTerm> terms) {
        return new ExactPattern(terms);
    }

    /**
     * Creates a resolution rule pattern that matches a prefix of predicate call.
     *
     * @param terms terms to match.
     *
     * @return new resolution rule pattern
     */
    public static Rule.Pattern prefixPattern(@Nonnull List<? extends PlainTerm> terms) {
        return new PrefixPattern(terms);
    }

    @Nonnull
    private final Pattern condition;

    @Nonnull
    private final Predicate predicate;

    private Rule(@Nonnull Pattern condition, @Nonnull Predicate predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    /**
     * A condition of this rule application.
     *
     * @return {@code condition} passed to constructor.
     */
    @Nonnull
    public final Pattern getCondition() {
        return this.condition;
    }

    /**
     * Predicate this rule resolves to when matched.
     *
     * @return {@code predicate} passed to constructor.
     */
    @Nonnull
    public final Predicate getPredicate() {
        return this.predicate;
    }

    /**
     * Attempts to match this given predicate {@code call} against this rule {@link #getCondition() condition}.
     *
     * @param call predicate call to match.
     * @param knowns known resolutions.
     *
     * @return rule match, or empty optional if the rule condition does not match.
     */
    public final Optional<Match> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns) {
        return getCondition()
                .match(call, knowns)
                .map(updatedKnowns -> new Rule.Match(this, updatedKnowns));
    }

    @Override
    public String toString() {
        return this.condition + " :- " + this.predicate;
    }

    /**
     * A selector of matching predicate resolution rules.
     */
    @FunctionalInterface
    @SamWithReceiver
    public interface Selector {

        /**
         * Selects resolution rules given predicate call matches.
         *
         * @param call predicate call.
         * @param knowns known resolutions.
         *
         * @return a {@link Flux} of {@link Match rule matches}.
         */
        @Nonnull
        Flux<Match> matchingRules(@Nonnull Predicate.Call call, @Nonnull Knowns knowns);

    }

    /**
     * Selected rule match.
     */
    @Immutable
    public static final class Match {

        @Nonnull
        private final Rule rule;

        @Nonnull
        private final Knowns knowns;

        Match(@Nonnull Rule rule, @Nonnull Knowns knowns) {
            this.rule = rule;
            this.knowns = knowns;
        }

        /**
         * Matching rule.
         */
        @Nonnull
        public final Rule getRule() {
            return this.rule;
        }

        /**
         * Variable mappings and resolutions returned from {@link Rule#getCondition() rule condition}
         * {@link Pattern#match(Predicate.Call, Knowns) match}.
         */
        @Nonnull
        public final Knowns getKnowns() {
            return this.knowns;
        }

        @Override
        public String toString() {
            return "Rule.Match{"
                    + "rule={" + this.rule + "}"
                    + ", knowns={" + this.knowns + "}"
                    + '}';
        }

    }

    /**
     * Resolution rule match pattern.
     *
     * <p>Patterns may be exact and prefix ones.</p>
     *
     * <p>Every predicate call term should match the exact patterns.</p>
     *
     * <p>Only the first {@code N} terms should match the prefix pattern, where {@code N} is equal to the number
     * of terms in the pattern.</p>
     */
    @Immutable
    public static abstract class Pattern {

        @Nonnull
        private final List<? extends PlainTerm> terms;

        /**
         * Constructs rule pattern.
         *
         * @param terms a list of terms this pattern consists of.
         */
        Pattern(@Nonnull List<? extends PlainTerm> terms) {
            this.terms = Collections.unmodifiableList(terms);
        }

        /**
         * A list of terms this pattern consists of.
         *
         * @return readonly list of terms.
         */
        @Nonnull
        public final List<? extends PlainTerm> getTerms() {
            return this.terms;
        }

        /**
         * Whether this is a prefix pattern.
         *
         * @return {@code true} if this is a prefix pattern, or {@code false} if this is an exact one.
         */
        public abstract boolean isPrefix();

        /**
         * Attempts to match the given predicate call against this pattern.
         *
         * <p>This method is called for the {@link Rule#getCondition() rule condition} with target call as argument.
         *
         * @param call a predicate call to match.
         * @param knowns known resolutions.
         *
         * @return updated knowns if the call matches this pattern, or empty optional otherwise.
         */
        @Nonnull
        public abstract Optional<Knowns> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns);

        /**
         * Creates predicate resolution rule with this pattern as its {@link Rule#getCondition() condition}.
         *
         * @param predicate predicate the constructed rule resolves to if this pattern matches.
         *
         * @return new predicate resolution rule.
         */
        @Nonnull
        public final Rule rule(@Nonnull Predicate predicate) {
            return new Rule(this, predicate);
        }

        /**
         * Creates a fact with this pattern as its {@link Rule#getCondition() condition}.
         *
         * @return new fact.
         */
        @Nonnull
        public final Rule fact() {
            return rule(Predicate.TRUE);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Pattern that = (Pattern) o;

            return this.terms.equals(that.terms);
        }

        @Override
        public int hashCode() {
            return this.terms.hashCode();
        }

    }

}
