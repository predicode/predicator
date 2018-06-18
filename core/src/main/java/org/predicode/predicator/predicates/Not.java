package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class Not implements Predicate {

    @Nonnull
    private final Predicate negated;

    Not(@Nonnull Predicate negated) {
        this.negated = negated;
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull Resolver resolver) {
        return this.negated.resolve(resolver)
                .next()
                .map(knowns -> true)
                .defaultIfEmpty(false)
                .filter(flag -> flag)
                .map(flag -> resolver.getKnowns())
                .flux();
    }

    @Override
    public String toString() {
        return "\\+ " + this.negated;
    }

}
