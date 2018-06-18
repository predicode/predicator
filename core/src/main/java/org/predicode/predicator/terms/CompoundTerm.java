package org.predicode.predicator.terms;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.RulePattern;

import javax.annotation.Nonnull;


/**
 * A compound term that may contain other terms.
 *
 * <p>Compound terms can not be part of {@link RulePattern rule patterns} and thus should be
 * {@link Term#expand(Predicate.Resolver) expanded} and replaced with {@link PlainTerm plain term}
 * (with {@link Variable#tempVariable(String) temporary variable} typically) prior to being matched.</p>
 */
public abstract class CompoundTerm extends Term {

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitCompound(this, p);
    }

}
