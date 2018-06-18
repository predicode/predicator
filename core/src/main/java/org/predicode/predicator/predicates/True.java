package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.PredicateResolver;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class True implements Predicate {

    static final True INSTANCE = new True();

    private True() {
    }

    @Nonnull
    @Override
    public Flux<Knowns> resolve(@Nonnull PredicateResolver resolver) {
        return Flux.just(resolver.getKnowns());
    }

    @Nonnull
    @Override
    public Predicate and(@Nonnull Predicate other) {
        return other;
    }

    @Nonnull
    @Override
    public Predicate or(@Nonnull Predicate other) {
        return this;
    }

    @Nonnull
    @Override
    public Predicate negate() {
        return FALSE;
    }

    @Override
    public String toString() {
        return ".";
    }

}
