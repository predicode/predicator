package org.predicode.predicator

import org.predicode.predicator.grammar.TermPrinter
import org.predicode.predicator.grammar.printTerms
import reactor.core.publisher.Flux
import java.util.function.UnaryOperator

/**
 * A phrase consisting of other terms.
 *
 * @constructor constructs new phrase.
 *
 * @property terms list of terms this phrase consists of.
 */
class Phrase(val terms: List<Term>) : CompoundTerm() {

    /**
     * Constructs new phrase out of terms array.
     *
     * @param terms array of terms this phrase consists of.
     */
    constructor(vararg terms: Term) : this(terms.asList())

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
     * Resolves this phrase as predicate.
     *
     * [expands][Term.expand] all of the phrase terms, then searches for corresponding [resolution rules][Rule]
     * and applies them.
     */
    fun resolve(resolver: PredicateResolver): Flux<Knowns> = try {
        expansion(resolver)?.resolve() ?: Flux.empty()
    } catch (e: Exception) {
        Flux.error(e)
    }

    override fun print(out: TermPrinter) {
        out.apply {
            startCompound()
            print(terms)
            endCompound()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Phrase

        if (terms != other.terms) return false

        return true
    }

    override fun hashCode(): Int {
        return terms.hashCode()
    }

    override fun toString() = printTerms(terms)

    private fun expansion(resolver: PredicateResolver): PhraseExpansion? {

        val expansion = PhraseExpansion(resolver, terms.size)

        terms.forEach { term ->
            if (!expansion.expandTerm(term)) return null
        }

        return expansion
    }

    private class PhraseExpansion(var resolver: PredicateResolver, size: Int) {

        private var predicate: Predicate = True
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

        fun resolve() = predicate().invoke(resolver)

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
