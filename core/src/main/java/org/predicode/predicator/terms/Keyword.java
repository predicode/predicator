package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.PredicateResolver;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.predicode.predicator.grammar.CodePoints.BACKTICK;
import static org.predicode.predicator.grammar.QuotingStyle.ALWAYS_QUOTE;


/**
 * Keyword term.
 *
 * <p>Keywords match only themselves. They can not be mapped to variables.</p>
 */
public abstract class Keyword extends PlainTerm {

    /**
     * A keyword designating definition of expression.
     *
     * <p>This is used to build phrase expansion rules. When expanding a phrase, it is replaced by (temporary) variable,
     * while predicate constructed to find a definition rule.</p>
     *
     * <p>A definition rule pattern consists of a variable, followed by this keyword, followed by expression terms.</p>
     */
    @Nonnull
    public static Keyword DEFINITION_KEYWORD = new Keyword(":=") {};

    /**
     * Creates a keyword with the given name.
     *
     * <p>This keyword matches only keywords constructed with this function with the same {@code name}</p>.
     *
     * @param name keyword name.
     */
    @Nonnull
    public static Keyword namedKeyword(@Nonnull String name) {
        return new NamedKeyword(name);
    }

    @Nonnull
    private final String name;

    /**
     * Constructs keyword.
     *
     * @param name keyword name.
     */
    public Keyword(@Nonnull String name) {
        this.name = name;
    }

    /**
     * Keyword name.
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
    public final Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
        return equals(term) ? Optional.of(knowns) : Optional.empty();
    }

    @Nonnull
    @Override
    public final Optional<Expansion> expand(@Nonnull PredicateResolver resolver) {
        return Optional.of(new Expansion(this, resolver.getKnowns()));
    }

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitKeyword(this, p);
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.keyword(getName());
    }

    @Override
    public String toString() {
        return ALWAYS_QUOTE.printName(getName(), BACKTICK);
    }

    private static final class NamedKeyword extends Keyword {

        NamedKeyword(@Nonnull String name) {
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

            final NamedKeyword that = (NamedKeyword) o;

            return getName().equals(that.getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

    }

}
