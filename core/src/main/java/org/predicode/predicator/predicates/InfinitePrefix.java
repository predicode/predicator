package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;


final class InfinitePrefix extends Predicate.Prefix {

    InfinitePrefix(
            @Nonnull List<? extends PlainTerm> terms,
            @Nonnull Predicate.Call suffix) {
        super(terms, suffix);
    }

    @Override
    public int length() {
        return -1;
    }

    @Override
    public String toString() {
        return printTerms(getTerms()) + " ... " + getRest();
    }

    @Nullable
    @Override
    FiniteCall toFinite() {
        return null;
    }

    @Nonnull
    @Override
    Optional<Predicate.Prefix> buildPrefix(int length) {

        final List<? extends PlainTerm> oldPrefix = getTerms();
        final int oldPrefixLen = oldPrefix.size();

        if (length == oldPrefixLen) {
            return Optional.of(this);
        }
        if (length < oldPrefixLen) {
            return Optional.of(new InfinitePrefix(
                    oldPrefix.subList(0, length),
                    new InfinitePrefix(oldPrefix.subList(length, oldPrefixLen), getRest())));
        }

        return getRest()
                .prefix(length - oldPrefixLen)
                .map(suffixPrefix -> {

                    final ArrayList<PlainTerm> newTerms = new ArrayList<>(length);

                    newTerms.addAll(oldPrefix);
                    newTerms.addAll(suffixPrefix.getTerms());

                    return Predicate.prefix(newTerms, suffixPrefix.getRest());
                });
    }

    @Nonnull
    @Override
    InfinitePrefix updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new InfinitePrefix(getTerms(), getRest().updateQualifiers(qualifiers));
    }

}
