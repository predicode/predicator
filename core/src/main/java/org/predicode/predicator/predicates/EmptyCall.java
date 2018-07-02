package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;


final class EmptyCall extends Predicate.Call implements FiniteCall {

    static final EmptyCall INSTANCE = new EmptyCall();

    private EmptyCall() {
    }

    @Nonnull
    @Override
    public final Map<? extends Qualifier.Signature, ? extends Qualifier> getQualifiers() {
        return emptyMap();
    }

    @Override
    public final int length() {
        return 0;
    }

    @Nonnull
    @Override
    public final Predicate.Call call() {
        return this;
    }

    @Nonnull
    @Override
    public List<? extends PlainTerm> allTerms() {
        return emptyList();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
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

    @Nonnull
    @Override
    public FinitePrefix updateQualifiers(@Nonnull Map<? extends Qualifier.Signature, ? extends Qualifier> qualifiers) {
        return new FinitePrefix(emptyList(), new QualifiedEmptyCall(qualifiers));
    }

}
