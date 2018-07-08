package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import org.predicode.predicator.terms.PlainTerm;
import org.predicode.predicator.terms.SignatureTerm;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;
import static org.predicode.predicator.terms.PlainTerm.matchTerms;


/**
 * Predicate qualifier.
 *
 * <p>Predicate may have arbitrary number of qualifiers, given each qualifier has unique {@link Signature signature}.
 * </p>
 */
public final class Qualifier {

    /**
     * Creates new qualifier.
     *
     * @param terms qualifier terms.
     *
     * @return new qualifier instance.
     */
    @Nonnull
    public static Qualifier of(@Nonnull PlainTerm... terms) {
        return of(Arrays.asList(terms));
    }

    /**
     * Creates new qualifier out of a list of terms.
     *
     * @param terms a list of qualifier terms.
     *
     * @return new qualifier instance.
     */
    @Nonnull
    public static Qualifier of(@Nonnull List<? extends PlainTerm> terms) {
        return new Qualifier(terms);
    }

    /**
     * Creates new qualifier signature.
     *
     * @param terms signature terms.
     *
     * @return new qualifier signature instance.
     */
    @Nonnull
    public static Signature signature(@Nonnull SignatureTerm... terms) {
        return signature(Arrays.asList(terms));
    }

    /**
     * Creates new qualifier signature out of a list of terms.
     *
     * @param terms a list of signature terms.
     *
     * @return new qualifier signature instance.
     */
    @Nonnull
    public static Signature signature(@Nonnull List<? extends SignatureTerm> terms) {
        return new Signature(terms);
    }

    @Nonnull
    private final List<? extends PlainTerm> terms;

    @Nonnull
    private final Signature signature;

    Qualifier(@Nonnull List<? extends PlainTerm> terms) {
        this.terms = unmodifiableList(terms);
        this.signature = signature(
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

    /**
     * Attempts to match the given predicate qualifier against this one.
     *
     * <p>This method is called for {@link Rule.Pattern#getQualifiers() each rule pattern qualifier} when
     * {@link Rule.Pattern#match(Predicate.Call, Knowns) matching} against {@link Predicate.Call#getQualifiers()
     * predicate call qualifiers}.</p>
     *
     * @param qualifier a predicate qualifier to match.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the qualifier matches this pattern, or empty optional otherwise.
     */
    @Nonnull
    public final Optional<Knowns> match(@Nonnull Qualifier qualifier, @Nonnull Knowns knowns) {
        return matchTerms(getTerms(), qualifier.getTerms(), knowns);
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

    /**
     * Qualifier signature.
     */
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
