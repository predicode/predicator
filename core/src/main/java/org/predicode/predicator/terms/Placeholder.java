package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.predicode.predicator.grammar.CodePoints.UNDERSCORE;


/**
 * Placeholder term.
 *
 * <p>It can be used everywhere the {@link Variable variable} can be used. But in contrast to variable, it is always
 * ignored.</p>
 */
public class Placeholder extends PlainTerm {

    private static final Placeholder PLACEHOLDER = new Placeholder();

    /**
     * Returns placeholder instance.
     *
     * @return singleton placeholder instance.
     */
    @Nonnull
    public static Placeholder placeholder() {
        return PLACEHOLDER;
    }

    private Placeholder() {
    }

    @Nonnull
    @Override
    public Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
        return term.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitMapped(@Nonnull MappedTerm term, @Nonnull Knowns knowns) {
                        return Optional.of(knowns);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitPlaceholder(@Nonnull Placeholder placeholder, @Nonnull Knowns knowns) {
                        return Optional.of(knowns);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitPlain(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
                        return Optional.empty();
                    }

                },
                knowns);
    }

    @Nonnull
    @Override
    public Optional<Expansion> expand(@Nonnull Predicate.Resolver resolver) {
        return Optional.of(new Expansion(this, resolver.getKnowns()));
    }

    @Nonnull
    @Override
    public <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitPlaceholder(this, p);
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.print(UNDERSCORE);
    }
}
