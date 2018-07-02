package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;
import org.predicode.predicator.terms.SignatureTerm;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;


/**
 * Predicate qualifier.
 *
 * <p>Predicate may have arbitrary number of qualifiers, given each qualifier has unique {@link Signature signature}.
 * </p>
 */
public final class Qualifier {

    @Nonnull
    private final List<? extends PlainTerm> terms;

    @Nonnull
    private final Signature signature;

    Qualifier(@Nonnull List<? extends PlainTerm> terms) {
        this.terms = unmodifiableList(terms);
        this.signature = new Signature(
                terms.stream()
                        .map(PlainTerm::getSignature)
                        .collect(Collectors.toList()));
    }

    @Nonnull
    public final List<? extends PlainTerm> getTerms() {
        return this.terms;
    }

    @Nonnull
    public final Signature getSignature() {
        return this.signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Qualifier qualifier = (Qualifier) o;

        return getTerms().equals(qualifier.getTerms());
    }

    @Override
    public int hashCode() {
        return getTerms().hashCode();
    }

    @Override
    public String toString() {
        return '@' + printTerms(getTerms());
    }

    public static final class Signature {

        @Nonnull
        private final List<? extends SignatureTerm> terms;

        Signature(@Nonnull List<? extends SignatureTerm> terms) {
            this.terms = unmodifiableList(terms);
        }

        @Nonnull
        public final List<? extends SignatureTerm> getTerms() {
            return this.terms;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Signature signature = (Signature) o;

            return getTerms().equals(signature.getTerms());
        }

        @Override
        public int hashCode() {
            return getTerms().hashCode();
        }

        @Override
        public String toString() {
            return '@' + printTerms(getTerms());
        }

    }

}
