package org.predicode.predicator.terms

import org.predicode.predicator.predicates.Qualifier
import org.predicode.predicator.predicates.QualifiersDsl

interface PhraseDsl : TermsDsl, QualifiersDsl

@Suppress("FunctionName")
fun Phrase(define: PhraseDsl.() -> Unit) = object : PhraseDsl {

    val terms = mutableListOf<Term>()
    val qualifiers = mutableListOf<Qualifier>()

    override fun term(term: Term) {
        terms.add(term)
    }

    override fun qualifier(qualifier: Qualifier) {
        qualifiers.add(qualifier)
    }

}.apply(define).let { Phrase(it.terms) }

fun TermsDsl.phrase(terms: PhraseDsl.() -> Unit) = Phrase(terms)
