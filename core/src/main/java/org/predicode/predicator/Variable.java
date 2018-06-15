package org.predicode.predicator;

import org.jetbrains.annotations.NotNull;
import org.predicode.predicator.grammar.NamePrinterKt;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.predicode.predicator.Keyword.DEFINITION_KEYWORD;
import static org.predicode.predicator.grammar.Characters.UNDERSCORE;
import static org.predicode.predicator.grammar.QuotingStyle.ALWAYS_QUOTE;


/**
 * Variable term.
 *
 * <p>Variable can be either local to rule, or global, i.e. present in original query. The former should be
 * {@link Knowns#map(Variable, MappedTerm) mapped} to their values prior to {@link Predicate#resolve(PredicateResolver)
 * predicate resolution}. All of the latter should be specified when {@link Knowns#Knowns(Variable...)
 * constructing knowns} and are to be {@link Knowns#resolve(Variable, ResolvedTerm)}.</p>
 */
public abstract class Variable extends MappedTerm {

    private static Random tempNameRandom = new Random();

    /**
     * Creates a named [variable][Variable].
     *
     * <p>This variable matches another one only if the latter is constructed with this function with the same
     * {@code name}.</p>
     *
     * @param name variable name.
     */
    @Nonnull
    public static Variable namedVariable(@Nonnull String name) {
        return new NamedVariable(name);
    }

    /**
     * Create temporary [variable][Variable].
     *
     * <p>Temporary variables are compared by their identity. In contrast to {@link #namedVariable(String)
     * named variables} the name of temporary one is used only for its representation.</p>
     *
     * @param prefix temporary variable name prefix.
     */
    @Nonnull
    public static Variable tempVariable(@Nonnull String prefix) {
        return new TempVariable(prefix + " " + tempNameRandom.nextInt());
    }

    @Nonnull
    private final String name;

    /**
     * Constructs variable.
     *
     * @param name variable name.
     */
    public Variable(@Nonnull String name) {
        this.name = name;
    }

    /**
     * Variable name.
     *
     * <p>This is used generally for representation only.</p>
     *
     * @return {@code name} passed to the constructor.
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    @Override
    public Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {

        final Variable self = this;

        return term.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitMapped(@Nonnull MappedTerm term, @Nonnull Knowns knowns) {
                        return knowns.map(self, term);
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
    public Optional<Expansion> expand(@Nonnull PredicateResolver resolver) {
        return resolver.getKnowns().mapping(this, Expansion::new);
    }

    @NotNull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitVariable(this, p);
    }

    @NotNull
    @Override
    public final <P, R> R accept(@Nonnull Term.Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitVariable(this, p);
    }

    /**
     * Builds rule pattern corresponding to definition of some expression.
     *
     * @param terms expression terms.
     *
     * @return rule pattern matching expression definition.
     */
    @Nonnull
    public RulePattern definitionOf(@Nonnull PlainTerm ...terms) {

        final ArrayList<PlainTerm> termList = new ArrayList<>(terms.length);

        termList.add(this);
        termList.add(DEFINITION_KEYWORD);
        termList.addAll(Arrays.asList(terms));

        return new RulePattern(termList);
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.variable(getName());
    }

    @Override
    public String toString() {
        return NamePrinterKt.printName(getName(), UNDERSCORE, ALWAYS_QUOTE);
    }

    private static final class NamedVariable extends Variable {

        NamedVariable(@Nonnull String name) {
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

            final NamedVariable that = (NamedVariable) o;

            return getName().equals(that.getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

    }

    private static final class TempVariable extends Variable {

        TempVariable(@Nonnull String name) {
            super(name);
        }

    }

}
