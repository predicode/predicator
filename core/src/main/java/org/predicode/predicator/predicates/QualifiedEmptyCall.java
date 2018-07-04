package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;


final class QualifiedEmptyCall extends Predicate.Call implements FiniteCall {

    @Nonnull
    private final Qualifiers qualifiers;

    QualifiedEmptyCall(@Nonnull Qualifiers qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    @Nonnull
    public Qualifiers getQualifiers() {
        return this.qualifiers;
    }

    @Nonnull
    @Override
    public Call call() {
        return this;
    }

    @Nonnull
    @Override
    public List<? extends PlainTerm> allTerms() {
        return emptyList();
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QualifiedEmptyCall that = (QualifiedEmptyCall) o;

        return this.qualifiers.equals(that.qualifiers);
    }

    @Override
    public int hashCode() {
        return this.qualifiers.hashCode();
    }

    @Override
    public String toString() {
        return this.qualifiers.toString();
    }

    @Nonnull
    @Override
    FiniteCall toFinite() {
        return this;
    }

    @Nonnull
    @Override
    Optional<Prefix> buildPrefix(int length) {
        if (length == 0) {
            return Optional.of(Predicate.prefix(emptyList(), this));
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public QualifiedEmptyCall updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new QualifiedEmptyCall(qualifiers);
    }

}
