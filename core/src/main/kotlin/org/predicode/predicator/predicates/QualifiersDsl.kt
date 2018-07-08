package org.predicode.predicator.predicates

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.terms.PlainTermsDsl
import org.predicode.predicator.terms.SignatureTermsDsl
import org.predicode.predicator.terms.plainTerms
import org.predicode.predicator.terms.signatureTerms

@Suppress("FunctionName")
fun Qualifier(define: PlainTermsDsl.() -> Unit) = Qualifier.of(plainTerms(define))

fun qualifierSignature(define: SignatureTermsDsl.() -> Unit) = Qualifier.signature(signatureTerms(define))

@PredicatorDslMarker
interface QualifiersDsl {

    fun qualifier(qualifier: Qualifier)

}

fun QualifiersDsl.q(define: PlainTermsDsl.() -> Unit) = qualifier(define)

fun QualifiersDsl.qualifier(define: PlainTermsDsl.() -> Unit) = Qualifier(define).also { qualifier(it) }

@Suppress("FunctionName")
fun Qualifiers(define: QualifiersDsl.() -> Unit) = object : QualifiersDsl {

    val qualifiers = mutableListOf<Qualifier>()

    override fun qualifier(qualifier: Qualifier) {
        qualifiers.add(qualifier)
    }

}.apply(define).let { Qualifiers.of(it.qualifiers) }
