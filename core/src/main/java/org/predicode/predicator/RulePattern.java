package org.predicode.predicator;

import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;


/**
 * Resolution rule match pattern.
 */
public final class RulePattern implements Predicate {

    @Nonnull
    private final List<? extends PlainTerm> terms;

    /**
     * Constructs rule pattern.
     *
     * @param terms a list of terms this pattern consists of.
     */
    public RulePattern(@Nonnull List<? extends PlainTerm> terms) {
        this.terms = Collections.unmodifiableList(terms);
    }

    /**
     * Constructs rule pattern out of terms array.
     *
     * @param terms array of terms this pattern consists of.
     */
    public RulePattern(@Nonnull PlainTerm ...terms) {
        this(Arrays.asList(terms));
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
     * Attempts to match against another pattern.
     *
     * This method is called for the {@link Rule#getCondition() rule condition} with query pattern as argument.
     *
     * @param pattern a pattern to match against.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the pattern matches, or empty optional otherwise.
     */
    @Nonnull
    public Optional<Knowns> match(@Nonnull RulePattern pattern, @Nonnull Knowns knowns) {
        if (pattern.getTerms().size() != getTerms().size()) {
            return Optional.empty();
        }

        @Nonnull
        Knowns result = knowns.startMatching();
        int index = 0;

        for (PlainTerm term : getTerms()) {

            final Optional<Knowns> match = term.match(pattern.getTerms().get(index), result);

            if (!match.isPresent()) {
                return Optional.empty();
            }

            result = match.get();
            ++index;
        }

        return Optional.of(result);
    }

    /**
     * Creates a resolution rule with this pattern as its {@link Rule#getCondition() condition}.
     *
     * @param predicate predicate the constructed rule resolves to if this pattern matches.
     */
    @Nonnull
    public final Rule rule(@Nonnull Predicate predicate) {
        return new Rule(this, predicate);
    }

    /**
     * Creates a fact with this pattern as its {@link Rule#getCondition() condition}.
     */
    @Nonnull
    public final Rule fact() {
        return rule(Predicate.TRUE);
    }

    /**
     * Creates a rule resolved by {@link #resolve(PredicateResolver) rules application}.
     *
     * @param terms terms the rule search pattern consists of.
     */
    @Nonnull
    public final Rule resolveBy(@Nonnull PlainTerm ...terms) {
        return rule(new RulePattern(terms));
    }

    /**
     * Creates a rule resolved by {@link Phrase#resolve(PredicateResolver) phrase predicate} consisting of the given
     * terms.
     *
     * @param terms terms the phrase consists of.
     */
    @Nonnull
    public final Rule resolveBy(@Nonnull Term ...terms) {
        return rule(new Phrase(terms));
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull PredicateResolver resolver) {
        return resolver.matchingRules(this, resolver.getKnowns())
                .flatMap(match -> match.getRule().getPredicate().resolve(resolver.withKnowns(match.getKnowns())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RulePattern that = (RulePattern) o;

        return this.terms.equals(that.terms);
    }

    @Override
    public int hashCode() {
        return this.terms.hashCode();
    }

    @Override
    public String toString() {
        return printTerms(getTerms());
    }

}
