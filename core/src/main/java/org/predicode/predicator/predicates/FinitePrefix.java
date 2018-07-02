package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;


final class FinitePrefix extends Predicate.Prefix implements FiniteCall {

    @Nonnull
    private final FiniteCall suffix;

    FinitePrefix(
            @Nonnull List<? extends PlainTerm> terms,
            @Nonnull FiniteCall suffix) {
        super(terms, suffix.call());
        this.suffix = suffix;
    }

    @Override
    public int length() {
        return getTerms().size() + suffixTerms().size();
    }

    @Nonnull
    @Override
    public Predicate.Call call() {
        return this;
    }

    @Nonnull
    @Override
    public List<? extends PlainTerm> allTerms() {
        if (getSuffix().isEmpty()) {
            return getTerms();
        }
        if (getTerms().isEmpty()) {
            return this.suffix.allTerms();
        }

        final ArrayList<PlainTerm> terms = new ArrayList<>();

        terms.addAll(getTerms());
        terms.addAll(this.suffix.allTerms());

        return terms;
    }

    @Override
    public String toString() {
        if (getSuffix().isEmpty()) {
            return printTerms(getTerms());
        }

        final StringBuilder out = new StringBuilder();

        printTerms(getTerms(), out);
        printTerms(suffixTerms(), out);
        if (getQualifiers().isEmpty()) {
            out.append(' ');
            getQualifiers().printQualifiers(out);
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
    Optional<Predicate.Prefix> buildPrefix(int length) {

        final List<? extends PlainTerm> oldPrefix = getTerms();
        final int oldPrefixLen = oldPrefix.size();

        if (length == oldPrefixLen) {
            return Optional.of(this);
        }

        final List<? extends PlainTerm> oldSuffix = suffixTerms();
        final int oldSuffixLen = oldSuffix.size();
        final int oldLength = oldPrefixLen + oldSuffixLen;

        if (length > oldLength) {
            return Optional.empty(); // Too many terms requested.
        }
        if (length == oldLength) {
            return Optional.of(new FinitePrefix(allTerms(), EmptyCall.INSTANCE));
        }

        final ArrayList<PlainTerm> newPrefix = new ArrayList<>(length);
        final ArrayList<PlainTerm> newSuffix = new ArrayList<>(oldLength - length);
        final int suffixToPrefix = length - oldPrefixLen;

        if (suffixToPrefix < 0) {
            newPrefix.addAll(oldPrefix.subList(0, length));
            newSuffix.addAll(oldPrefix.subList(length, oldPrefixLen));
            newSuffix.addAll(oldSuffix);
        } else {
            newPrefix.addAll(oldPrefix);
            newPrefix.addAll(oldSuffix.subList(0, suffixToPrefix));
            newSuffix.addAll(oldSuffix.subList(suffixToPrefix, oldSuffixLen));
        }

        return Optional.of(new FinitePrefix(newPrefix, new FinitePrefix(newSuffix, EmptyCall.INSTANCE)));
    }

    @Nonnull
    private List<? extends PlainTerm> suffixTerms() {
        return this.suffix.allTerms();
    }

    @Nonnull
    @Override
    public FinitePrefix updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new FinitePrefix(getTerms(), this.suffix.updateQualifiers(qualifiers));
    }

}
