package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import org.predicode.predicator.grammar.TermPrinter;
import org.predicode.predicator.predicates.Predicate;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.predicode.predicator.grammar.QuotedName.VARIABLE_NAME;
import static org.predicode.predicator.grammar.QuotingStyle.ALWAYS_QUOTE;


/**
 * Variable term.
 *
 * <p>Variable can be either local to rule, or global, i.e. present in original query. The former should be
 * {@link Knowns#map(Variable, MappedTerm) mapped} to their values prior to {@link Predicate#resolve(Predicate.Resolver)
 * predicate resolution}. All of the latter should be specified when {@link Knowns#Knowns(Variable...)
 * constructing knowns} and are to be {@link Knowns#resolve(Variable, ResolvedTerm)}.</p>
 */
public abstract class Variable extends MappedTerm {

    private static Random tempNameRandom = new Random();

    /**
     * Creates a named {@link Variable variable}.
     *
     * <p>This variable matches another one only if the latter is constructed with this function with the same
     * {@code name}.</p>
     *
     * @param name variable name.
     */
    @Nonnull
    public static Variable named(@Nonnull String name) {
        return new NamedVariable(name);
    }

    /**
     * Create temporary {@link Variable variable}.
     *
     * <p>Temporary variables are compared by their identity. In contrast to {@link #named(String)
     * named variables} the name of temporary one is used only for its representation.</p>
     *
     * @param prefix temporary variable name prefix.
     */
    @Nonnull
    public static Variable temp(@Nonnull String prefix) {
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

    @Nonnull
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
    public Flux<Expansion> expand(@Nonnull Predicate.Resolver resolver) {
        return Flux.just(resolver.getKnowns().<Expansion>mapping(this, Expansion::new));
    }

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
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
    public final Rule.Pattern definitionOf(@Nonnull PlainTerm ...terms) {
        return Rule.pattern(definitionTerms(terms));
    }

    /**
     * Builds pattern call corresponding to definition of some expression.
     *
     * @param terms expression terms.
     *
     * @return rule pattern matching expression definition.
     */
    @Nonnull
    public final Predicate.Call definitionCall(@Nonnull PlainTerm ...terms) {
        return Predicate.call(definitionTerms(terms));
    }

    @Nonnull
    private ArrayList<PlainTerm> definitionTerms(@Nonnull PlainTerm[] terms) {

        final ArrayList<PlainTerm> termList = new ArrayList<>(2 + terms.length);

        termList.add(this);
        termList.add(Keyword.definition());
        termList.addAll(Arrays.asList(terms));

        return termList;
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.variable(getName());
    }

    @Override
    public String toString() {
        return ALWAYS_QUOTE.printName(getName(), VARIABLE_NAME);
    }

}
