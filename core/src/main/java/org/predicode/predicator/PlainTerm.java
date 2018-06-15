package org.predicode.predicator;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * A plain, non-compound term.
 *
 * <p>{@link RulePattern Rule patterns} may contain plain terms only.</p>
 *
 * <p>Can be one of:
 * <ul>
 * <li>{@link Keyword keyword},</li>
 * <li>{@link Atom atom},</li>
 * <li>{@link Value arbitrary value}, or</li>
 * <li>{@link Variable variable}.</li>
 * </ul>
 * </p>
 */
public abstract class PlainTerm extends Term {

    PlainTerm() {
    }

    /**
     * Attempts to match against another term.
     *
     * This method is called for the terms of the [rule condition][Rule.condition] with corresponding query term
     * as argument.
     *
     * @param term a term to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or empty optional otherwise.
     */
    public abstract Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns);

    @Nonnull
    public abstract <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p);

    interface Visitor<P, R> {

        @Nonnull
        default R visitKeyword(@Nonnull Keyword keyword, @Nonnull P p) {
            return visitPlain(keyword, p);
        }

        @Nonnull
        default R visitAtom(@Nonnull Atom atom, @Nonnull P p) {
            return visitResolved(atom, p);
        }

        @Nonnull
        default R visitVariable(@Nonnull Variable variable, @Nonnull P p) {
            return visitMapped(variable, p);
        }

        @Nonnull
        default R visitValue(@Nonnull Value value, @Nonnull P p) {
            return visitResolved(value, p);
        }

        @Nonnull
        default R visitMapped(@Nonnull MappedTerm term, @Nonnull P p) {
            return visitPlain(term, p);
        }

        @Nonnull
        default R visitResolved(@Nonnull ResolvedTerm term, @Nonnull P p) {
            return visitMapped(term, p);
        }

        @Nonnull
        R visitPlain(@Nonnull PlainTerm term, @Nonnull P p);

    }

}
