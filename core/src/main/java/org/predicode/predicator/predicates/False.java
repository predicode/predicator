package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class False implements Predicate {

    static final False INSTANCE = new False();

    private False() {
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull Resolver resolver) {
        return Flux.empty();
    }

    @Nonnull
    @Override
    public Predicate and(@Nonnull Predicate other) {
        return this;
    }

    @Nonnull
    @Override
    public Predicate or(@Nonnull Predicate other) {
        return other;
    }

    @Nonnull
    @Override
    public Predicate negate() {
        return TRUE;
    }

    @Override
    public String toString() {
        return "\\+.";
    }

}
