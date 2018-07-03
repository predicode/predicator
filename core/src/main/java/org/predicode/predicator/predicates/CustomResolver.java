package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;


final class CustomResolver implements Predicate.Resolver {

    @Nonnull
    private final Knowns knowns;

    @Nonnull
    private final Rule.Selector selector;

    CustomResolver(@Nonnull Knowns knowns, @Nonnull Rule.Selector selector) {
        this.knowns = knowns;
        this.selector = selector;
    }

    @Nonnull
    @Override
    public Knowns getKnowns() {
        return this.knowns;
    }

    @Nonnull
    @Override
    public Flux<Rule.Match> matchingRules(@Nonnull Predicate.Call call) {
        return this.selector.matchingRules(call, getKnowns());
    }

}
