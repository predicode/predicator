package org.predicode.predicator;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;


/**
 * Resolution rule match pattern.
 */
public final class RulePattern {

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
    public RulePattern(@Nonnull PlainTerm... terms) {
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
    public Optional<Knowns> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns) {
        if (!call.isFinite()) {
            // TODO implement infinite predicate call matching against open rule patterns.
            return Optional.empty();
        }
        if (getTerms().size() != call.length()) {
            return Optional.empty(); // Wrong call length
        }

        return call.prefix(getTerms().size()).flatMap(prefix -> {

            @Nonnull
            Knowns result = knowns.startMatching();
            int index = 0;

            for (PlainTerm term : getTerms()) {

                final Optional<Knowns> match = term.match(prefix.getTerms().get(index), result);

                if (!match.isPresent()) {
                    return Optional.empty();
                }

                result = match.get();
                ++index;
            }

            return Optional.of(result);
        });
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
