package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.PredicateResolver;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class And implements Predicate {

    @Nonnull
    private final Predicate first;

    @Nonnull
    private final Predicate second;

    And(@Nonnull Predicate first, @Nonnull Predicate second) {
        this.first = first;
        this.second = second;
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull PredicateResolver resolver) {
        return this.first.resolve(resolver)
                .flatMap(resolved -> this.second.resolve(resolver.withKnowns(resolved)));
    }

    @Override
    public String toString() {
        return this.first + ", " + this.second;
    }

}
