package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.*
import java.util.function.UnaryOperator

class PhraseTest {

    @Test
    fun `string representation`() {
        assert(Phrase(namedKeyword("keyword"), namedVariable("variable")).toString())
                .toBe("keyword _variable")
    }

    @Nested
    inner class PhrasePredicate {

        @Test
        fun `expands terms`() {

            val knowns = Knowns()
            val resolver = object : PredicateResolver {
                override fun getKnowns() = knowns
                override fun matchingRules(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match> =
                        selectOneOf()(pattern, knowns)
            }
            val term = mockk<PlainTerm>()

            every { term.expand(refEq(resolver)) }.returns(Optional.of(Term.Expansion(term, knowns)))

            StepVerifier.create(Phrase(term).resolve(resolver))
                    .verifyComplete()

            verify { term.expand(refEq(resolver)) }
        }

        @Test
        fun `updates predicate`() {

            val knowns = Knowns()
            val resolver = object : PredicateResolver {
                override fun getKnowns() = knowns
                override fun matchingRules(pattern: RulePattern, knowns: Knowns): Flux<Rule.Match> =
                        selectOneOf()(pattern, knowns)
            }
            val term = mockk<PlainTerm>("term")
            val predicate = mockk<Predicate>("predicate")
            val and = mockk<Predicate>("AND")
            val updatePredicate = mockk<UnaryOperator<Predicate>>()

            every { term.expand(any()) }.returns(Optional.of(Term.Expansion(term, knowns, updatePredicate)))
            every { updatePredicate.apply(any()) }.returns(predicate)
            every { predicate.and(any()) }.returns(and)
            every { and(any()) }.returns(knowns.toMono().toFlux())

            StepVerifier.create(Phrase(term).resolve(resolver))
                    .consumeNextWith {}
                    .verifyComplete()

            verify {
                updatePredicate.apply(any())
                predicate.and(any())
                and(any())
            }
        }

    }

}
