package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class Or implements Predicate {

    @Nonnull
    private final Predicate first;

    @Nonnull
    private final Predicate second;

    Or(@Nonnull Predicate first, @Nonnull Predicate second) {
        this.first = first;
        this.second = second;
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull Resolver resolver) {
        return Flux.merge(this.first.resolve(resolver), this.second.resolve(resolver));
    }

    @Override
    public String toString() {
        return this.first + "; " + this.second;
    }

}
