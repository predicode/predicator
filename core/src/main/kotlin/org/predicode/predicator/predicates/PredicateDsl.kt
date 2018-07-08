package org.predicode.predicator.predicates

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.terms.PlainTerm
import org.predicode.predicator.terms.PlainTermsDsl

@PredicatorDslMarker
interface PredicateDsl {

    @PredicatorDslMarker
    interface CallDsl : PlainTermsDsl, QualifiersDsl

}

internal class PredicateCallBuilder : PredicateDsl.CallDsl {

    val terms = mutableListOf<PlainTerm>()
    val qualifiers = mutableListOf<Qualifier>()

    override fun term(term: PlainTerm) {
        terms.add(term)
    }

    override fun qualifier(qualifier: Qualifier) {
        qualifiers.add(qualifier)
    }

}

fun predicateCall(define: PredicateDsl.CallDsl.() -> Unit) =
        PredicateCallBuilder()
                .apply(define)
                .let { Predicate.call(it.terms).qualify(Qualifiers.of(it.qualifiers)) }
