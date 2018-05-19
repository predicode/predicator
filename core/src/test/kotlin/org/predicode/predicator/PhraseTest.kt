package org.predicode.predicator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.function.UnaryOperator


class PhraseTest {

    @Nested
    inner class PhrasePredicate {

        @Test
        fun `expands terms`() {

            val knowns = Knowns()
            val resolver = object : PredicateResolver {
                override val knowns = knowns
                override val ruleSelector = ruleSelector()
            }
            val term = mockk<PlainTerm>()

            every { term.expand(refEq(resolver)) }.returns(Term.Expansion(term))

            StepVerifier.create(Phrase(term).predicate().resolve(resolver))
                    .verifyComplete()

            verify { term.expand(refEq(resolver)) }
        }

        @Test
        fun `updates predicate`() {

            val knowns = Knowns()
            val resolver = object : PredicateResolver {
                override val knowns = knowns
                override val ruleSelector = ruleSelector()
            }
            val term = mockk<PlainTerm>("term")
            val predicate = mockk<Predicate>("predicate")
            val and = mockk<Predicate>("and")
            val updatePredicate = mockk<UnaryOperator<Predicate>>()

            every { term.expand(refEq(resolver)) }.returns(Term.Expansion(term, updatePredicate))
            every { updatePredicate.apply(any()) }.returns(predicate)
            every { predicate.and(any()) }.returns(and)
            every { and.resolve(any()) }.returns(knowns.toMono().toFlux())

            StepVerifier.create(Phrase(term).predicate().resolve(resolver))
                    .expectNext(knowns)
                    .verifyComplete()

            verify {
                updatePredicate.apply(any())
                predicate.and(any())
                and.resolve(refEq(resolver))
            }
        }

    }

}
