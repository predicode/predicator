package org.predicode.predicator.predicates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.IntFunction;


final class InfiniteCall extends Predicate.Call {

    @Nonnull
    private final IntFunction<Optional<Predicate.Prefix>> buildPrefix;

    InfiniteCall(@Nonnull IntFunction<Optional<Predicate.Prefix>> buildPrefix) {
        this.buildPrefix = buildPrefix;
    }

    @Override
    public int length() {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InfiniteCall that = (InfiniteCall) o;

        return this.buildPrefix.equals(that.buildPrefix);
    }

    @Override
    public int hashCode() {
        return this.buildPrefix.hashCode();
    }

    @Override
    public String toString() {
        return this.buildPrefix.toString();
    }

    @Nullable
    @Override
    FiniteCall toFinite() {
        return null;
    }

    @Nonnull
    @Override
    Optional<Predicate.Prefix> buildPrefix(int length) {
        return this.buildPrefix.apply(length);
    }

}
