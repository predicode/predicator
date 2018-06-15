package org.predicode.predicator;

import org.jetbrains.annotations.NotNull;
import org.predicode.predicator.grammar.NamePrinterKt;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.predicode.predicator.grammar.Characters.SINGLE_QUOTE;
import static org.predicode.predicator.grammar.QuotingStyle.ALWAYS_QUOTE;


/**
 * Atom term.
 *
 * <p>Atoms match only themselves and can be mapped to variables.</p>
 */
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

    @NotNull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitAtom(this, p);
    }

    @NotNull
    @Override
    public final <P, R> R accept(@Nonnull Term.Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitAtom(this, p);
    }

    @Override
    public final Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {

        final Atom self = this;

        return term.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @NotNull
                    @Override
                    public Optional<Knowns> visitAtom(@Nonnull Atom atom, @NotNull Knowns knowns) {
                        return self.equals(atom) ? Optional.of(knowns) : Optional.empty();
                    }

                    @NotNull
                    @Override
                    public Optional<Knowns> visitVariable(@Nonnull Variable variable, @NotNull Knowns knowns) {
                        return knowns.resolve(variable, self);
                    }

                    @NotNull
                    @Override
                    public Optional<Knowns> visitPlain(@Nonnull PlainTerm term, @NotNull Knowns knowns) {
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
        return NamePrinterKt.printName(getName(), SINGLE_QUOTE, ALWAYS_QUOTE);
    }

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
