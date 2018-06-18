package org.predicode.predicator;

import org.predicode.predicator.annotations.SamWithReceiver;
import org.predicode.predicator.predicates.Predicate;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * Predicate resolution rule.
 *
 * <p>The {@link Knowns known mappings} returned by {@link #getCondition() condition} are passed to
 * {@link #getPredicate() predicate}. They should be compatible.</p>
 */
public final class Rule {

    @Nonnull
    private final RulePattern condition;

    @Nonnull
    private final Predicate predicate;

    /**
     * Constructs predicate resolution rule.
     *
     * @param condition a condition this rule matches.
     * @param predicate predicate this rule resolves to when matched.
     */
    public Rule(@Nonnull RulePattern condition, @Nonnull Predicate predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    /**
     * A condition this rule matches.
     *
     * @return {@code condition} passed to constructor.
     */
    @Nonnull
    public final RulePattern getCondition() {
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
         * {@link RulePattern#match(Predicate.Call, Knowns) match}.
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

}
