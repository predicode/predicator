package org.predicode.predicator;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * A plain term a query {@link Variable variable} may resolve to.
 */
public abstract class ResolvedTerm extends MappedTerm {

    ResolvedTerm() {
    }

    @Nonnull
    @Override
    public final Optional<Expansion> expand(@Nonnull PredicateResolver resolver) {
        return Optional.of(new Expansion(this, resolver.getKnowns()));
    }

}
