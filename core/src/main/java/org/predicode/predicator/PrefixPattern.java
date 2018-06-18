package org.predicode.predicator;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;

import static org.predicode.predicator.grammar.TermPrinter.printTerms;


@Immutable
final class PrefixPattern extends Rule.Pattern {

    @Nonnull
    static Optional<Knowns> matchPrefix(
            @Nonnull Predicate.Prefix prefix,
            @Nonnull List<? extends PlainTerm> terms,
            @Nonnull Knowns knowns) {

        @Nonnull
        Knowns result = knowns.startMatching();
        int index = 0;

        for (PlainTerm term : terms) {

            final Optional<Knowns> match = term.match(prefix.getTerms().get(index), result);

            if (!match.isPresent()) {
                return Optional.empty();
            }

            result = match.get();
            ++index;
        }

        return Optional.of(result);
    }

    PrefixPattern(@Nonnull List<? extends PlainTerm> terms) {
        super(terms);
    }

    @Override
    public boolean isPrefix() {
        return true;
    }

    @Nonnull
    @Override
    public Optional<Knowns> match(@Nonnull Predicate.Call call, @Nonnull Knowns knowns) {
        return call.prefix(getTerms().size())
                .flatMap(prefix -> matchPrefix(prefix, getTerms(), knowns));
    }

    @Override
    public String toString() {
        return printTerms(getTerms()) + "...";
    }

}
