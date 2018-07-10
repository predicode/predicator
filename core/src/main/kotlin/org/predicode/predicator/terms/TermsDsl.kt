package org.predicode.predicator.terms

import org.predicode.predicator.annotations.PredicatorDslMarker

@PredicatorDslMarker
interface ResolvedTermsDsl {

    fun term(term: ResolvedTerm)

    @JvmDefault
    fun terms(vararg terms: ResolvedTerm) {
        terms.forEach { term(it) }
    }

}

fun newResolvedTerms(define: ResolvedTermsDsl.() -> Unit): List<ResolvedTerm> =
        object : ResolvedTermsDsl {

            val terms = mutableListOf<ResolvedTerm>()

            override fun term(term: ResolvedTerm) {
                terms.add(term)
            }

        }.apply(define).terms


@PredicatorDslMarker
interface MappedTermsDsl : ResolvedTermsDsl {

    fun term(term: MappedTerm)

    @JvmDefault
    fun terms(vararg terms: MappedTerm) {
        terms.forEach { term(it) }
    }

    override fun term(term: ResolvedTerm) {
        term(term as MappedTerm)
    }

}

fun newMappedTerms(define: MappedTermsDsl.() -> Unit): List<MappedTerm> =
        object : MappedTermsDsl {

            val terms = mutableListOf<MappedTerm>()

            override fun term(term: MappedTerm) {
                terms.add(term)
            }

        }.apply(define).terms


@PredicatorDslMarker
interface SignatureTermsDsl {

    fun term(term: SignatureTerm)

    @JvmDefault
    fun terms(vararg terms: SignatureTerm) {
        terms.forEach { term(it) }
    }

}

fun newSignatureTerms(define: SignatureTermsDsl.() -> Unit): List<SignatureTerm> =
        object : SignatureTermsDsl {

            val terms = mutableListOf<SignatureTerm>()

            override fun term(term: SignatureTerm) {
                terms.add(term)
            }

        }.apply(define).terms

@PredicatorDslMarker
interface PlainTermsDsl : MappedTermsDsl, SignatureTermsDsl {

    fun term(term: PlainTerm)

    fun terms(vararg terms: PlainTerm) {
        terms.forEach { term(it) }
    }

    override fun term(term: MappedTerm) {
        term(term as PlainTerm)
    }

    override fun term(term: SignatureTerm) {
        term(term as PlainTerm)
    }

}

fun newPlainTerms(define: PlainTermsDsl.() -> Unit): List<PlainTerm> =
        object : PlainTermsDsl {

            val terms = mutableListOf<PlainTerm>()

            override fun term(term: PlainTerm) {
                terms.add(term)
            }

        }.apply(define).terms


@PredicatorDslMarker
interface TermsDsl : PlainTermsDsl {

    fun term(term: Term)

    @JvmDefault
    fun terms(vararg terms: Term) {
        terms.forEach { term(it) }
    }

    @JvmDefault
    override fun term(term: PlainTerm) {
        term(term as Term)
    }

}

fun newTerms(define: TermsDsl.() -> Unit): List<Term> =
        object : TermsDsl {

            val terms = mutableListOf<Term>()

            override fun term(term: Term) {
                terms.add(term)
            }

        }.apply(define).terms

fun SignatureTermsDsl.k(name: String) = keyword(name)

fun SignatureTermsDsl.keyword(name: String) = Keyword.named(name).also { term(it) }

val SignatureTermsDsl.p get() = placeholder

val SignatureTermsDsl.placeholder get() = Placeholder.placeholder().also { term(it) }

fun MappedTermsDsl.v(name: String) = variable(name)

fun MappedTermsDsl.variable(name: String) = Variable.named(name).also { term(it) }

fun ResolvedTermsDsl.a(name: String) = atom(name)

fun ResolvedTermsDsl.atom(name: String) = Atom.named(name).also { term(it) }

fun <T : Any> ResolvedTermsDsl.raw(value: T) = Value.raw(value).also { term(it) }
