package org.predicode.predicator

import reactor.core.publisher.Flux
import java.util.*
import java.util.function.UnaryOperator

/**
 * A phrase consisting of other terms.
 *
 * @param terms terms this phrase consists of.
 */
class Phrase(private vararg val terms: Term) : CompoundTerm(), List<Term> by terms.asList() {

    override fun expand(resolver: PredicateResolver): Expansion? =
            expansion(resolver)?.let { expansion ->
                tempVariable("phrase expansion").let { tempVar ->
                    Expansion(
                            tempVar,
                            expansion.resolver.knowns,
                            UnaryOperator { expansion.definition(tempVar) and it })
                }
            }

    /**
     * Creates a phrase predicate that [expands][Term.expand] all of its terms, then searches for corresponding
     * [resolution rules][Rule], and applies them.
     */
    fun predicate() = object : Predicate {

        override fun resolve(resolver: PredicateResolver): Flux<Knowns> = try {
            expansion(resolver)?.resolve() ?: Flux.empty()
        } catch (e: Exception) {
            Flux.error(e)
        }

        override fun toString() = this@Phrase.toString()

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Phrase

        return Arrays.equals(terms, other.terms)
    }

    override fun hashCode() = Arrays.hashCode(terms)

    override fun toString() = terms.joinToString(" ") { it.toPhraseString() }

    override fun toPhraseString() = "($this)"

    private fun expansion(resolver: PredicateResolver): PhraseExpansion? {

        val expansion = PhraseExpansion(resolver, terms.size)

        terms.forEach { term ->
            if (!expansion.expandTerm(term)) return null
        }

        return expansion
    }

    private class PhraseExpansion(var resolver: PredicateResolver, size: Int) {

        private var predicate: Predicate = alwaysTrue()
        private val terms: Array<PlainTerm?> = arrayOfNulls(size)
        private var index = 0

        fun expandTerm(term: Term): Boolean =
                term.expand(resolver)
                        ?.let { (expanded, knowns, updatePredicate) ->
                            terms[index++] = expanded
                            predicate = updatePredicate.apply(predicate)
                            resolver = resolver.withKnowns(knowns)
                            true
                        }
                        ?: false

        fun resolve() = predicate().resolve(resolver)

        fun definition(variable: Variable): Predicate =
                resolver.knowns.declareLocal(variable) { local, knowns ->
                    resolver = resolver.withKnowns(knowns)
                    predicate and local.definitionOf(*expandedTerms()).applyRules()
                }

        private fun predicate() = predicate and RulePattern(*expandedTerms()).applyRules()

        @Suppress("UNCHECKED_CAST")
        private fun expandedTerms() = terms as Array<out PlainTerm>

    }

}