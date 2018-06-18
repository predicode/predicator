package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assert
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
        assert(call.isEmpty).isTrue()
    }

    @Test
    fun `is finite`() {
        assert(call.isFinite).isTrue()
        assert(call.toFinite()).isNotNull {
            assert(subject).isSame(call)
            assert(subject.allTerms()).isEmpty()
        }
    }

    @Test
    fun `has empty prefix`() {
        assert(call.prefix(0).get()) {
            assert(subject.terms).isEmpty()
            assert(subject.suffix.isEmpty).isTrue()
            assert(subject.isEmpty).isTrue()
        }
    }

    @Test
    fun `can not build non-empty prefix`() {
        assert(call.prefix(1)).toBe(Optional.empty())
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
        assert(Predicate.call(emptyList())).isSame(emptyCall())
    }

    @Test
    fun `is empty prefix with terms`() {
        assert(call).toBe(Predicate.prefix(terms))
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
        assert(call.isEmpty).isFalse()
        assert(call.length()).notToBe(0)
    }

    @Test
    fun `is infinite`() {
        assert(call.isFinite).isFalse()
        assert(call.toFinite()).isNull()
        assert(call.length()).isLessThan(0)
    }

    @Test
    fun `builds prefix with the given function`() {

        val prefix = Predicate.prefix(
                listOf(
                        namedKeyword("keyword"),
                        namedAtom("atom"),
                        rawValue(System.currentTimeMillis())))

        every { buildPrefix.apply(any()) }.returns(Optional.of(prefix))

        assert(call.prefix(3).get()).toBe(prefix)
        verify { buildPrefix.apply(3) }
    }

}
