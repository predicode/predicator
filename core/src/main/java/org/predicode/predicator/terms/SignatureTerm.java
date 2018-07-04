package org.predicode.predicator.terms;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * A plain term that can be part of predicate or qualifier signature.
 *
 * <p>Each plain term has its {@link PlainTerm#getSignature() signature}. This can be either:
 * <ul>
 *     <li>a keyword itself, or</li>
 *     <li>a placeholder for all other terms.</li>
 * </ul>
 * </p>
 */
@Immutable
public abstract class SignatureTerm extends PlainTerm {

    SignatureTerm() {
    }

    @Nonnull
    @Override
    public final SignatureTerm getSignature() {
        return this;
    }

    @Nonnull
    public abstract <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p);

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull PlainTerm.Visitor<P, R> visitor, @Nonnull P p) {
        return accept((Visitor<P, R>) visitor, p);
    }

    public interface Visitor<P, R> extends MappedTerm.Visitor<P, R> {

        @Nonnull
        default R visitKeyword(@Nonnull Keyword keyword, @Nonnull P p) {
            return visitSignature(keyword, p);
        }

        @Nonnull
        default R visitPlaceholder(@Nonnull Placeholder placeholder, @Nonnull P p) {
            return visitSignature(placeholder, p);
        }

        @Nonnull
        R visitSignature(@Nonnull SignatureTerm term, @Nonnull P p);

    }

}
