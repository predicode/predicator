package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import org.predicode.predicator.grammar.TermPrinter;
import org.predicode.predicator.predicates.Predicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;
import static org.predicode.predicator.terms.Variable.temp;


/**
 * A phrase consisting of other terms.
 */
@Immutable
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
    public Mono<Expansion> expand(@Nonnull Resolver resolver) {
        return expansion(resolver)
                .map(phraseExpansion -> {

                    final Variable tempVar = temp("phrase expansion");

                    return phraseExpansion.resolver.getKnowns().declareLocal(
                            tempVar,
                            (local, knowns) -> new Expansion(
                                    tempVar,
                                    knowns,
                                    predicate -> phraseExpansion.definition(local).and(predicate)));
                })
                .next();
    }

    /**
     * Resolves this phrase as predicate.
     *
     * <p>{@link Term#expand(Resolver) Expands} all of the phrase terms, then searches for corresponding
     * {@link Rule resolution rules} and applies them.</p>
     */
    @Override
    @Nonnull
    public Flux<Knowns> resolve(@Nonnull Resolver resolver) {
        try {
            return expansion(resolver)
                    .flatMap(PhraseExpansion::resolve);
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
    private Flux<PhraseExpansion> expansion(@Nonnull Resolver resolver) {
        return Flux.fromIterable(getTerms())
                .reduce(
                        Flux.just(new PhraseExpansion(resolver, getTerms().size())),
                        (expansions, term) -> expansions.flatMap(expansion -> expansion.expandTerm(term)))
                .flux()
                .flatMap(expansions -> expansions);
    }

    @Immutable
    private static final class PhraseExpansion {

        @Nonnull
        private final Predicate.Resolver resolver;

        @Nonnull
        private final Predicate predicate;

        @Nonnull
        private final PlainTerm[] terms;

        private final int index;

        PhraseExpansion(@Nonnull Predicate.Resolver resolver, int size) {
            this.resolver = resolver;
            this.predicate = Predicate.TRUE;
            this.terms = new PlainTerm[size];
            this.index = 0;
        }

        private PhraseExpansion(@Nonnull PhraseExpansion prev, @Nonnull Term.Expansion termExpansion) {
            this.resolver = prev.resolver.withKnowns(termExpansion.getKnowns());
            this.predicate = termExpansion.getUpdatePredicate().apply(prev.predicate);
            this.terms = prev.terms;
            this.index = prev.index + 1;
            this.terms[prev.index] = termExpansion.getExpanded();

        }

        @Nonnull
        Flux<PhraseExpansion> expandTerm(@Nonnull Term term) {
            return term
                    .expand(this.resolver)
                    .flux()
                    .map(termExpansion -> new PhraseExpansion(this, termExpansion));
        }

        @Nonnull
        Flux<Knowns> resolve() {
            return predicate().resolve(this.resolver);
        }

        @Nonnull
        Predicate definition(@Nonnull Variable local) {
            return this.predicate.and(local.definitionCall(this.terms));
        }

        @Nonnull
        private Predicate predicate() {
            return this.predicate.and(Predicate.call(Arrays.asList(this.terms)));
        }

    }

}
