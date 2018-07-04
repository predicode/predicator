package org.predicode.predicator.predicates;

import org.predicode.predicator.terms.PlainTerm;

import javax.annotation.Nonnull;
import java.util.List;


interface FiniteCall {

    @Nonnull
    Predicate.Call call();

    @Nonnull
    List<? extends PlainTerm> allTerms();

    @Nonnull
    FiniteCall updateQualifiers(@Nonnull Qualifiers qualifiers);

}
