package org.predicode.predicator;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.predicates.Qualifiers;
import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;
import static org.predicode.predicator.terms.PlainTerm.matchTerms;


@Immutable
final class PrefixPattern extends Rule.Pattern {

    @Nonnull
    static Optional<Knowns> matchPrefix(
            @Nonnull Predicate.Prefix prefix,
            @Nonnull Rule.Pattern pattern,
            @Nonnull Knowns knowns) {
        return matchTerms(pattern.getTerms(), prefix.getTerms(), knowns.startMatching())
                .flatMap(updated -> pattern.getQualifiers().match(prefix.getQualifiers(), updated));
    }

    PrefixPattern(@Nonnull List<? extends PlainTerm> terms) {
        super(terms);
    }

    private PrefixPattern(
            @Nonnull List<? extends PlainTerm> terms,
            @Nonnull Qualifiers qualifiers) {
        super(terms, qualifiers);
    }

    @Override
    public boolean isPrefix() {
        return true;
    }

    @Nonnull
    @Override
    public Optional<Knowns> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns) {
        return call.prefix(getTerms().size())
                .flatMap(prefix -> matchPrefix(prefix, this, knowns));
    }

    @Override
    public String toString() {

        final StringBuilder out = new StringBuilder();

        printTerms(getTerms());
        out.append("...");
        if (!getQualifiers().isEmpty()) {
            out.append(' ');
            getQualifiers().printQualifiers(out);
        }

        return out.toString();
    }

    @Nonnull
    @Override
    PrefixPattern updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new PrefixPattern(getTerms(), qualifiers);
    }

}
