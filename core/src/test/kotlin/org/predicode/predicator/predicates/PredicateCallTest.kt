package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.verbs.assertThat
import ch.tutteli.atrium.verbs.expect
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.predicates.Predicate.emptyCall
import org.predicode.predicator.terms.PlainTerm
import org.predicode.predicator.terms.namedAtom
import org.predicode.predicator.terms.namedKeyword
import org.predicode.predicator.terms.rawValue
import org.predicode.predicator.testutils.isEmpty
import java.util.*
import java.util.function.IntFunction

class EmptyCallTest {

    lateinit var call: Predicate.Call

    @BeforeEach
    fun create() {
        call = emptyCall()
    }

    @Test
    fun `is empty`() {
        assertThat(call.isEmpty).toBe(true)
    }

    @Test
    fun `is finite`() {
        assertThat(call.isFinite).toBe(true)
        assertThat(call.toFinite()).notToBeNull {
            isSameAs(call)
            assertThat(subject.allTerms()).isEmpty()
        }
    }

    @Test
    fun `has empty prefix`() {
        assertThat(call.prefix(0).get()) {
            assertThat(subject.terms).isEmpty()
            assertThat(subject.suffix.isEmpty).toBe(true)
            assertThat(subject.isEmpty).toBe(true)
        }
    }

    @Test
    fun `can not build non-empty prefix`() {
        assertThat(call.prefix(1)).isEmpty()
    }

    @Test
    fun `fails on invalid prefix request`() {
        expect { call.prefix(-1) }
                .toThrow<IllegalArgumentException> {}
    }

}

class FiniteCallTest {

    lateinit var terms: List<PlainTerm>
    lateinit var call: Predicate.Call

    @BeforeEach
    fun create() {
        terms = listOf(namedKeyword("keyword"), namedAtom("atom"), rawValue(System.currentTimeMillis()))
        call = Predicate.call(terms)
    }

    @Test
    fun `is empty without terms`() {
        assertThat(Predicate.call(emptyList())).isSameAs(emptyCall())
    }

    @Test
    fun `is empty prefix with terms`() {
        assertThat(call).toBe(Predicate.prefix(terms))
    }

}

class InfiniteCallTest {

    lateinit var buildPrefix: IntFunction<Optional<Predicate.Prefix>>
    lateinit var call: Predicate.Call

    @BeforeEach
    fun create() {
        buildPrefix = mockk("buildPrefix")
        call = Predicate.infiniteCall(buildPrefix)
    }

    @Test
    fun `is not empty`() {
        assertThat(call.isEmpty).toBe(false)
        assertThat(call.length()).notToBe(0)
    }

    @Test
    fun `is infinite`() {
        assertThat(call.isFinite).toBe(false)
        assertThat(call.toFinite()).toBe(null)
        assertThat(call.length()).isLessThan(0)
    }

    @Test
    fun `builds prefix with the given function`() {

        val prefix = Predicate.prefix(
                listOf(
                        namedKeyword("keyword"),
                        namedAtom("atom"),
                        rawValue(System.currentTimeMillis())))

        every { buildPrefix.apply(any()) }.returns(Optional.of(prefix))

        assertThat(call.prefix(3).get()).toBe(prefix)
        verify { buildPrefix.apply(3) }
    }

}
