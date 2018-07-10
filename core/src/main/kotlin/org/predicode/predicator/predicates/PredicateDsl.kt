package org.predicode.predicator.predicates

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.terms.PlainTerm
import org.predicode.predicator.terms.PlainTermsDsl

@PredicatorDslMarker
interface PredicateDsl {

    @PredicatorDslMarker
    interface Call : PlainTermsDsl, QualifiersDsl

}

internal class PredicateCallBuilder : PredicateDsl.Call {

    val terms = mutableListOf<PlainTerm>()
    val qualifiers = mutableListOf<Qualifier>()

    override fun term(term: PlainTerm) {
        terms.add(term)
    }

    override fun qualifier(qualifier: Qualifier) {
        qualifiers.add(qualifier)
    }

}

fun newPredicateCall(define: PredicateDsl.Call.() -> Unit) =
        PredicateCallBuilder()
                .apply(define)
                .let { Predicate.call(it.terms).qualify(Qualifiers.of(it.qualifiers)) }
