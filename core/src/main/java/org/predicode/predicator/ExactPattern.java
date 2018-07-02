package org.predicode.predicator;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.predicates.Qualifiers;
import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.PrefixPattern.matchPrefix;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;


@Immutable
final class ExactPattern extends Rule.Pattern {

    ExactPattern(@Nonnull List<? extends PlainTerm> terms) {
        super(terms);
    }

    private ExactPattern(
            @Nonnull List<? extends PlainTerm> terms,
            @Nonnull Qualifiers qualifiers) {
        super(terms, qualifiers);
    }

    @Override
    public boolean isPrefix() {
        return false;
    }

    @Override
    @Nonnull
    public Optional<Knowns> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns) {
        return call.prefix(getTerms().size())
                .filter(prefix -> prefix.getSuffix().isEmpty()) // Ensure the call length equals to pattern one
                .flatMap(prefix -> matchPrefix(prefix, this, knowns));
    }

    @Override
    public String toString() {

        final StringBuilder out = new StringBuilder();

        printTerms(getTerms());
        if (!getQualifiers().isEmpty()) {
            out.append(' ');
            getQualifiers().printQualifiers(out);
        }

        return out.toString();
    }

    @Nonnull
    @Override
    ExactPattern updateQualifiers(@Nonnull Qualifiers qualifiers) {
        return new ExactPattern(getTerms(), qualifiers);
    }

}
