package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.grammar.QuotedName;
import org.predicode.predicator.grammar.TermPrinter;
import org.predicode.predicator.predicates.Predicate;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.predicode.predicator.grammar.QuotedName.*;
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
     * @param name keyword name or sign.
     *
     * @return new {@link Kind#KEYWORD ordinal} keyword.
     */
    @Nonnull
    public static Keyword namedKeyword(@Nonnull String name) {
        return new NamedKeyword(name, Kind.KEYWORD);
    }

    /**
     * Creates a prefix operator with the given name.
     *
     * <p>This keyword matches only keywords constructed with this function with the same {@code name}</p>.
     *
     * @param name keyword name or sign.
     *
     * @return new {@link Kind#PREFIX_OPERATOR prefix operator}.
     */
    @Nonnull
    public static Keyword prefixOperator(@Nonnull String name) {
        return new NamedKeyword(name, Kind.PREFIX_OPERATOR);
    }

    /**
     * Creates an infix operator with the given name.
     *
     * <p>This keyword matches only keywords constructed with this function with the same {@code name}</p>.
     *
     * @param name keyword name or sign.
     *
     * @return new {@link Kind#INFIX_OPERATOR infix operator}.
     */
    @Nonnull
    public static Keyword infixOperator(@Nonnull String name) {
        return new NamedKeyword(name, Kind.INFIX_OPERATOR);
    }

    @Nonnull
    private final String name;

    @Nonnull
    private final Kind kind;

    /**
     * Constructs keyword.
     *
     * @param name keyword name or sign.
     */
    public Keyword(@Nonnull String name) {
        this(name, Kind.KEYWORD);
    }

    /**
     * Constructs keyword or operator.
     *
     * @param name keyword name or sign.
     * @param kind keyword kind.
     */
    public Keyword(@Nonnull String name, @Nonnull Kind kind) {
        this.name = name;
        this.kind = kind;
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

    /**
     * A kind of this keyword.
     *
     * @return keyword {@code kind} passed to the constructor.
     */
    @Nonnull
    public Kind getKind() {
        return this.kind;
    }

    @Nonnull
    @Override
    public final Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
        return equals(term) ? Optional.of(knowns) : Optional.empty();
    }

    @Nonnull
    @Override
    public final Optional<Expansion> expand(@Nonnull Predicate.Resolver resolver) {
        return Optional.of(new Expansion(this, resolver.getKnowns()));
    }

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitKeyword(this, p);
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.keyword(getName(), getKind());
    }

    @Override
    public String toString() {
        return ALWAYS_QUOTE.printName(getName(), getKind().getQuoted());
    }

    /**
     * A kind of keyword.
     *
     * <p>This is used to distinguish keywords from operators.</p>
     */
    public enum Kind {

        /**
         * Ordinary keyword.
         *
         * <p>Such keywords are always separated from sibling terms with spaces.</p>
         */
        KEYWORD(KEYWORD_NAME),

        /**
         * Prefix operator.
         *
         * <p>An operator which name (or sign) is not separated from the next term.</p>
         */
        PREFIX_OPERATOR(PREFIX_OPERATOR_NAME),

        /**
         * Infix operator.
         *
         * <p>An operator whish name (or sign) is not separated from previous term.</p>
         */
        INFIX_OPERATOR(INFIX_OPERATOR_NAME);

        @Nonnull
        private final QuotedName quoted;

        Kind(@Nonnull QuotedName quoted) {
            this.quoted = quoted;
        }

        /**
         * A quoted name used for the keyword of this kind.
         */
        @Nonnull
        public final QuotedName getQuoted() {
            return this.quoted;
        }

    }

    private static final class NamedKeyword extends Keyword {

        NamedKeyword(@Nonnull String name, @Nonnull Kind kind) {
            super(name, kind);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final NamedKeyword operator = (NamedKeyword) o;

            if (!getName().equals(operator.getName())) {
                return false;
            }

            return getKind() == operator.getKind();
        }

        @Override
        public int hashCode() {
            int result = getName().hashCode();
            result = 31 * result + getKind().hashCode();
            return result;
        }

    }

}
