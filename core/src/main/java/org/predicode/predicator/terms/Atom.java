package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.predicode.predicator.Knowns;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.predicode.predicator.grammar.QuotedName.ATOM_NAME;
import static org.predicode.predicator.grammar.QuotingStyle.ALWAYS_QUOTE;


/**
 * Atom term.
 *
 * <p>Atoms match only themselves and can be mapped to variables.</p>
 */
@Immutable
public abstract class Atom extends ResolvedTerm {

    /**
     * Creates an atom with the given name.
     *
     * <p>This atom matches another one only if the latter is constructed with this function and has the same
     * {@code name}.</p>
     *
     * @param name atom name.
     */
    @Nonnull
    public static Atom namedAtom(@Nonnull String name) {
        return new NamedAtom(name);
    }

    @Nonnull
    private final String name;

    /**
     * Constructs atom.
     *
     * @param name atom name.
     */
    public Atom(@Nonnull String name) {
        this.name = name;
    }

    /**
     * Atom name.
     *
     * <p>This is used generally for representation only.</p>
     *
     * @return {@code name} passed to the constructor.
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull ResolvedTerm.Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitAtom(this, p);
    }

    @Nonnull
    @Override
    public final Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {

        final Atom self = this;

        return term.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitAtom(@Nonnull Atom atom, @Nonnull Knowns knowns) {
                        return self.equals(atom) ? Optional.of(knowns) : Optional.empty();
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitVariable(@Nonnull Variable variable, @Nonnull Knowns knowns) {
                        return knowns.resolve(variable, self);
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

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.atom(getName());
    }

    @Override
    public String toString() {
        return ALWAYS_QUOTE.printName(getName(), ATOM_NAME);
    }

    @Immutable
    private static final class NamedAtom extends Atom {

        NamedAtom(@Nonnull String name) {
            super(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Atom.NamedAtom that = (Atom.NamedAtom) o;

            return getName().equals(that.getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

    }


}
