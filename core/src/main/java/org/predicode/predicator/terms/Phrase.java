package org.predicode.predicator.terms;

import org.predicode.predicator.*;
import org.predicode.predicator.grammar.TermPrinter;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.terms.Variable.tempVariable;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;


/**
 * A phrase consisting of other terms.
 */
public class Phrase extends CompoundTerm implements Predicate {

    @Nonnull
    private final List<? extends Term> terms;

    /**
     * Constructs phrase.
     *
     * @param terms list of terms this phrase consists of.
     */
    public Phrase(@Nonnull List<? extends Term> terms) {
        this.terms = Collections.unmodifiableList(terms);
    }

    /**
     * Constructs phrase out of terms array.
     *
     * @param terms array of terms this phrase consists of.
     */
    public Phrase(@Nonnull Term ...terms) {
        this(Arrays.asList(terms));
    }

    /**
     * List of terms this phrase consists of.
     *
     * @return readonly list.
     */
    @Nonnull
    public final List<? extends Term> getTerms() {
        return this.terms;
    }

    @Nonnull
    @Override
    public Optional<Expansion> expand(@Nonnull PredicateResolver resolver) {
        return expansion(resolver)
                .map(expansion -> {

                    final Variable tempVar = tempVariable("phrase expansion");

                    return new Expansion(
                            tempVar,
                            expansion.resolver.getKnowns(),
                            predicate -> expansion.definition(tempVar).and(predicate));
                });
    }

    /**
     * Resolves this phrase as predicate.
     *
     * <p>{@link Term#expand(PredicateResolver) Expands} all of the phrase terms, then searches for corresponding
     * {@link Rule resolution rules} and applies them.</p>
     */
    @Nonnull
    public Flux<Knowns> resolve(@Nonnull PredicateResolver resolver) {
        try {
            return expansion(resolver)
                    .map(PhraseExpansion::resolve)
                    .orElse(Flux.empty());
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.startCompound();
        out.print(getTerms());
        out.endCompound();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Phrase phrase = (Phrase) o;

        return this.terms.equals(phrase.terms);
    }

    @Override
    public int hashCode() {
        return this.terms.hashCode();
    }

    @Override
    public String toString() {
        return printTerms(getTerms());
    }

    @Nonnull
    private Optional<PhraseExpansion> expansion(@Nonnull PredicateResolver resolver) {

        final PhraseExpansion expansion = new PhraseExpansion(resolver, getTerms().size());

        for (final Term term : getTerms()) {
            if (!expansion.expandTerm(term)) {
                return Optional.empty();
            }
        }

        return Optional.of(expansion);
    }

    private static final class PhraseExpansion {

        @Nonnull
        private PredicateResolver resolver;

        @Nonnull
        private Predicate predicate = Predicate.TRUE;

        @Nonnull
        private PlainTerm[] terms;

        private int index = 0;

        PhraseExpansion(@Nonnull PredicateResolver resolver, int size) {
            this.resolver = resolver;
            this.terms = new PlainTerm[size];
        }

        boolean expandTerm(@Nonnull Term term) {
            return term
                    .expand(this.resolver)
                    .map(termExpansion -> {
                        this.terms[this.index++] = termExpansion.getExpanded();
                        this.predicate = termExpansion.getUpdatePredicate().apply(this.predicate);
                        this.resolver = this.resolver.withKnowns(termExpansion.getKnowns());
                        return true;
                    })
                    .orElse(false);
        }

        @Nonnull
        Flux<Knowns> resolve() {
            return predicate().resolve(this.resolver);
        }

        @Nonnull
        Predicate definition(@Nonnull Variable variable) {
            return this.resolver.getKnowns().declareLocal(variable, (local, knowns) -> {
                this.resolver = this.resolver.withKnowns(knowns);
                return this.predicate.and(local.definitionOf(this.terms));
            });
        }

        @Nonnull
        private Predicate predicate() {
            return this.predicate.and(new RulePattern(this.terms));
        }

    }

}
