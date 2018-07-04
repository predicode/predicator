package org.predicode.predicator.predicates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.IntFunction;

import static org.predicode.predicator.predicates.Qualifiers.noQualifiers;


final class InfiniteCall extends Predicate.Call {

    @Nonnull
    private final IntFunction<Optional<Predicate.Prefix>> buildPrefix;

    @Nonnull
    private final Qualifiers qualifiers;

    InfiniteCall(@Nonnull IntFunction<Optional<Predicate.Prefix>> buildPrefix) {
        this(buildPrefix, noQualifiers());
    }

    InfiniteCall(
            @Nonnull IntFunction<Optional<Prefix>> buildPrefix,
            @Nonnull Qualifiers qualifiers) {
        this.buildPrefix = buildPrefix;
        this.qualifiers = qualifiers;
    }

    @Override
    @Nonnull
    public final Qualifiers getQualifiers() {
        return this.qualifiers;
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
        if (!super.equals(o)) {
            return false;
        }

        final InfiniteCall that = (InfiniteCall) o;

        return this.buildPrefix.equals(that.buildPrefix);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        result = 31 * result + this.buildPrefix.hashCode();

        return result;
    }

    @Override
    public String toString() {

        final StringBuilder out = new StringBuilder();

        out.append(this.buildPrefix);
        if (!this.qualifiers.isEmpty()) {
            out.append(' ');
            this.qualifiers.printQualifiers(out);
        }

        return out.toString();
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

    @Nonnull
    @Override
    InfiniteCall updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new InfiniteCall(this.buildPrefix, qualifiers);
    }

}
