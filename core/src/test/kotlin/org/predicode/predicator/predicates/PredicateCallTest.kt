package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assertThat
import ch.tutteli.atrium.expect
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
        assertThat(call.isEmpty).isTrue()
    }

    @Test
    fun `is finite`() {
        assertThat(call.isFinite).isTrue()
        assertThat(call.toFinite()).isNotNull {
            assertThat(subject).isSame(call)
            assertThat(subject.allTerms()).isEmpty()
        }
    }

    @Test
    fun `has empty prefix`() {
        assertThat(call.prefix(0).get()) {
            assertThat(subject.terms).isEmpty()
            assertThat(subject.suffix.isEmpty).isTrue()
            assertThat(subject.isEmpty).isTrue()
        }
    }

    @Test
    fun `can not build non-empty prefix`() {
        assertThat(call.prefix(1)).toBe(Optional.empty())
    }

    @Test
    fun `fails on invalid prefix request`() {
        expect { call.prefix(-1) }
                .toThrow<IllegalArgumentException>()
    }

}

class FiniteCallTest {

    lateinit var terms: List<PlainTerm>;
    lateinit var call: Predicate.Call

    @BeforeEach
    fun create() {
        terms = listOf(namedKeyword("keyword"), namedAtom("atom"), rawValue(System.currentTimeMillis()))
        call = Predicate.call(terms)
    }

    @Test
    fun `is empty without terms`() {
        assertThat(Predicate.call(emptyList())).isSame(emptyCall())
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
        assertThat(call.isEmpty).isFalse()
        assertThat(call.length()).notToBe(0)
    }

    @Test
    fun `is infinite`() {
        assertThat(call.isFinite).isFalse()
        assertThat(call.toFinite()).isNull()
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
