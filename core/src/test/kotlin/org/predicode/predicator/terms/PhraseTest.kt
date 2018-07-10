package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.predicode.predicator.Knowns
import org.predicode.predicator.Rule
import org.predicode.predicator.invoke
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.selectOneOf
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.*
import java.util.function.UnaryOperator

class PhraseTest {

    @Test
    fun `string representation`() {
        assertThat(
                Phrase {
                    k("keyword")
                    v("variable")
                }.toString())
                .toBe("keyword _variable")
    }

    @Nested
    inner class PhrasePredicate {

        @Test
        fun `expands terms`() {

            val knowns = Knowns.none()
            val resolver = object : Predicate.Resolver {
                override fun getKnowns() = knowns
                override fun matchingRules(call: Predicate.Call): Flux<Rule.Match> =
                        selectOneOf()(call, knowns)
            }
            val term = mockk<PlainTerm>()

            every { term.expand(refEq(resolver)) }.returns(Optional.of(Term.Expansion(term, knowns)))

            StepVerifier.create(Phrase(term).resolve(resolver))
                    .verifyComplete()

            verify { term.expand(refEq(resolver)) }
        }

        @Test
        fun `updates predicate`() {

            val knowns = Knowns.none()
            val resolver = object : Predicate.Resolver {
                override fun getKnowns() = knowns
                override fun matchingRules(call: Predicate.Call): Flux<Rule.Match> =
                        selectOneOf()(call, knowns)
            }
            val term = mockk<PlainTerm>("term")
            val predicate = mockk<Predicate>("predicate")
            val and = mockk<Predicate>("AND")
            val updatePredicate = mockk<UnaryOperator<Predicate>>()

            every { term.expand(any()) }.returns(Optional.of(Term.Expansion(term, knowns, updatePredicate)))
            every { updatePredicate.apply(any()) }.returns(predicate)
            every { predicate.and(any()) }.returns(and)
            every { and.resolve(any()) }.returns(knowns.toMono().toFlux())

            StepVerifier.create(Phrase(term).resolve(resolver))
                    .consumeNextWith {}
                    .verifyComplete()

            verify {
                updatePredicate.apply(any())
                predicate.and(any())
                and.resolve(any())
            }
        }

    }

}
