package org.predicode.predicator;

import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


/**
 * Predicate resolver to resolve predicates against.
 *
 * <p>Instances of this class expected to be immutable.</p>
 */
public interface PredicateResolver {

    /**
     * Known variable mappings and resolutions.
     */
    @Nonnull
    Knowns getKnowns();

    /**
     * Selects matching predicate resolution rules.
     *
     * @param pattern rule search pattern.
     * @param knowns known resolutions.
     *
     * @return a {@link Flux} of {@link Rule.Match rule matches}.
     */
    @Nonnull
    Flux<Rule.Match> matchingRules(@Nonnull RulePattern pattern, @Nonnull Knowns knowns);

    /**
     * Constructs new predicate resolver based on this one with the given variable mappings an resolutions.
     *
     * @param knowns new variable mappings an resolutions.
     */
    default PredicateResolver withKnowns(@Nonnull final Knowns knowns) {
        return new PredicateResolver() {

            @Nonnull
            @Override
            public Knowns getKnowns() {
                return knowns;
            }

            @Nonnull
            @Override
            public Flux<Rule.Match> matchingRules(@Nonnull RulePattern pattern, @Nonnull Knowns knowns) {
                return PredicateResolver.this.matchingRules(pattern, knowns);
            }

        };

    }

}
