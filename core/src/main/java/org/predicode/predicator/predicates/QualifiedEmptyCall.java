package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;


final class QualifiedEmptyCall extends Predicate.Call implements FiniteCall {

    @Nonnull
    private final Map<? extends Qualifier.Signature, ? extends Qualifier> qualifiers;

    QualifiedEmptyCall(@Nonnull Map<? extends Qualifier.Signature, ? extends Qualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    @Nonnull
    public final Map<? extends Qualifier.Signature, ? extends Qualifier> getQualifiers() {
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

        final StringBuilder out = new StringBuilder();
        int i = 0;

        for (final Qualifier qualifier : this.qualifiers.values()) {
            if (i != 0) {
                out.append(' ');
            }
            out.append('@');
            ++i;
            printTerms(qualifier.getTerms(), out);
        }

        return out.toString();
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
    public QualifiedEmptyCall updateQualifiers(
            @Nonnull Map<? extends Qualifier.Signature, ? extends Qualifier> qualifiers) {
        return new QualifiedEmptyCall(qualifiers);
    }

}
