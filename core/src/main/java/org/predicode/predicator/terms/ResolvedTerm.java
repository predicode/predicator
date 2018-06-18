package org.predicode.predicator.terms;

import org.predicode.predicator.PredicateResolver;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * A plain term a query {@link Variable variable} may resolve to.
 */
public abstract class ResolvedTerm extends MappedTerm {

    ResolvedTerm() {
    }

    @Nonnull
    public abstract <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p);

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull MappedTerm.Visitor<P, R> visitor, @Nonnull P p) {
        return accept((Visitor<P, R>) visitor, p);
    }

    @Nonnull
    @Override
    public final Optional<Expansion> expand(@Nonnull PredicateResolver resolver) {
        return Optional.of(new Expansion(this, resolver.getKnowns()));
    }

    public interface Visitor<P, R> {

        @Nonnull
        default R visitAtom(@Nonnull Atom atom, @Nonnull P p) {
            return visitResolved(atom, p);
        }

        @Nonnull
        default R visitValue(@Nonnull Value value, @Nonnull P p) {
            return visitResolved(value, p);
        }

        @Nonnull
        R visitResolved(@Nonnull ResolvedTerm term, @Nonnull P p);

    }

}
