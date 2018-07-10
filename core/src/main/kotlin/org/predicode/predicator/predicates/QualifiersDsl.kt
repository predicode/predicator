package org.predicode.predicator.predicates

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.terms.PlainTermsDsl
import org.predicode.predicator.terms.SignatureTermsDsl
import org.predicode.predicator.terms.newPlainTerms
import org.predicode.predicator.terms.newSignatureTerms

fun newQualifier(define: PlainTermsDsl.() -> Unit) = Qualifier.of(newPlainTerms(define))

fun newQualifierSignature(define: SignatureTermsDsl.() -> Unit) = Qualifier.signature(newSignatureTerms(define))

@PredicatorDslMarker
interface QualifiersDsl {

    fun qualifier(qualifier: Qualifier)

}

fun QualifiersDsl.q(define: PlainTermsDsl.() -> Unit) = qualifier(define)

fun QualifiersDsl.qualifier(define: PlainTermsDsl.() -> Unit) = newQualifier(define).also { qualifier(it) }

fun newQualifiers(define: QualifiersDsl.() -> Unit) = object : QualifiersDsl {

    val qualifiers = mutableListOf<Qualifier>()

    override fun qualifier(qualifier: Qualifier) {
        qualifiers.add(qualifier)
    }

}.apply(define).let { Qualifiers.of(it.qualifiers) }
