package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * A plain, non-compound term.
 *
 * <p>{@link Rule.Pattern Rule patterns} may contain plain terms only.</p>
 *
 * <p>Can be one of:
 * <ul>
 * <li>{@link Keyword keyword},</li>
 * <li>{@link Atom atom},</li>
 * <li>{@link Value arbitrary value},</li>
 * <li>{@link Variable variable}, or</li>
 * <li>{@link Placeholder placeholder}.</li>
 * </ul>
 * </p>
 */
@Immutable
public abstract class PlainTerm extends Term {

    PlainTerm() {
    }

    /**
     * Returns this term's signature.
     */
    @Nonnull
    public abstract SignatureTerm getSignature();

    /**
     * Attempts to match against another term.
     *
     * <p>This method is called for the terms of the {@link Rule#getCondition() rule condition} with corresponding
     * query term as argument.</p>
     *
     * @param term a term to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or empty optional otherwise.
     */
    @Nonnull
    public abstract Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns);

    @Nonnull
    public abstract <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p);

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull Term.Visitor<P, R> visitor, @Nonnull P p) {
        return accept((Visitor<P, R>) visitor, p);
    }

    public interface Visitor<P, R> extends MappedTerm.Visitor<P, R>, SignatureTerm.Visitor<P, R> {

        @Nonnull
        @Override
        default R visitSignature(@Nonnull SignatureTerm term, @Nonnull P p) {
            return visitPlain(term, p);
        }

        @Override
        @Nonnull
        default R visitMapped(@Nonnull MappedTerm term, @Nonnull P p) {
            return visitPlain(term, p);
        }

        @Nonnull
        R visitPlain(@Nonnull PlainTerm term, @Nonnull P p);

    }

}
