package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.predicode.predicator.Knowns;
import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.grammar.TermPrinter;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;


/**
 * Basic term.
 *
 * <p>Can be one of:
 * <ul>
 * <li>{@link PlainTerm plain term}, or</li>
 * <li>{@link CompoundTerm compound term}, such as {@link Phrase phrase}</li>
 * </ul>
 * </p>
 */
@Immutable
public abstract class Term {

    Term() {
    }

    /**
     * Expands this term replacing it with plain one.
     *
     * <p>Expansion happens e.g. when resolving a {@link Phrase phrase predicate}.</p>
     *
     * <p>Plain terms typically expand to themselves, except for {@link Variable variables} that are expanded to their
     * {@link Knowns#mapping(Variable, BiFunction) mappings}.
     *
     * @param resolver predicate resolver instance to resolve/expand against.
     *
     * @return this term expansion(s), or empty flux if this term can not be expanded.
     */
    @Nonnull
    public abstract Mono<Expansion> expand(@Nonnull Predicate.Resolver resolver);

    /**
     * Prints this term representation with the given term printer.
     *
     * @param out term printer to print this term representation with.
     */
    public abstract void print(@Nonnull TermPrinter out);

    @Nonnull
    public abstract <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p);

    @Override
    public String toString() {
        return TermPrinter.printTerms(this);
    }

    /**
     * Term expansion.
     *
     * <p>This is a result of term {@link #expand(Predicate.Resolver) expansion}, when expansion is possible.</p>
     */
    public static final class Expansion {

        @Nonnull
        private final PlainTerm expanded;

        @Nonnull
        private final Knowns knowns;

        @Nonnull
        private final UnaryOperator<Predicate> updatePredicate;

        public Expansion(
                @Nonnull PlainTerm expanded,
                @Nonnull Knowns knowns) {
            this.expanded = expanded;
            this.knowns = knowns;
            this.updatePredicate = UnaryOperator.identity();
        }

        public Expansion(
                @Nonnull PlainTerm expanded,
                @Nonnull Knowns knowns,
                @Nonnull UnaryOperator<Predicate> updatePredicate) {
            this.expanded = expanded;
            this.knowns = knowns;
            this.updatePredicate = updatePredicate;
        }

        @Nonnull
        public final PlainTerm getExpanded() {
            return this.expanded;
        }

        @Nonnull
        public final Knowns getKnowns() {
            return this.knowns;
        }

        @Nonnull
        public final UnaryOperator<Predicate> getUpdatePredicate() {
            return this.updatePredicate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Expansion expansion = (Expansion) o;

            if (!this.expanded.equals(expansion.expanded)) {
                return false;
            }
            if (!this.knowns.equals(expansion.knowns)) {
                return false;
            }

            return this.updatePredicate.equals(expansion.updatePredicate);
        }

        @Override
        public int hashCode() {

            int result = this.expanded.hashCode();

            result = 31 * result + this.knowns.hashCode();
            result = 31 * result + this.updatePredicate.hashCode();

            return result;
        }

        @Override
        public String toString() {
            return "Expansion{"
                    + "expanded=" + this.expanded
                    + ", knowns=" + this.knowns
                    + ", updatePredicate=" + this.updatePredicate
                    + '}';
        }

    }

    public interface Visitor<P, R> extends PlainTerm.Visitor<P, R> {

        @Nonnull
        R visitCompound(@Nonnull CompoundTerm term, @Nonnull P p);

    }

}
