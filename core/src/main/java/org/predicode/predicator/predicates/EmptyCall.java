package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;


final class EmptyCall extends Predicate.Call implements FiniteCall {

    static final EmptyCall INSTANCE = new EmptyCall();

    private EmptyCall() {
    }

    @Override
    public int length() {
        return 0;
    }

    @Nonnull
    @Override
    public Predicate.Call call() {
        return this;
    }

    @Nonnull
    @Override
    public List<? extends PlainTerm> allTerms() {
        return emptyList();
    }

    @Override
    public String toString() {
        return "()";
    }

    @Nonnull
    @Override
    FiniteCall toFinite() {
        return this;
    }

    @Nonnull
    @Override
    Optional<Predicate.Prefix> buildPrefix(int length) {
        if (length == 0) {
            return Optional.of(Predicate.prefix(emptyList(), this));
        }
        return Optional.empty();
    }

}
