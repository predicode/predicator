package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assertThat
import ch.tutteli.atrium.expect
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.PlainTerm
import org.predicode.predicator.terms.namedKeyword
import java.util.*
import java.util.function.IntFunction

class FinitePrefixTest {

    lateinit var suffixTerms: List<PlainTerm>
    lateinit var suffix: Predicate.Call
    lateinit var prefixTerms: List<PlainTerm>
    lateinit var prefix: Predicate.Prefix

    @BeforeEach
    fun create() {
        suffixTerms = listOf(namedKeyword("suffix1"), namedKeyword("suffix2"))
        suffix = Predicate.call(suffixTerms)
        prefixTerms = listOf(namedKeyword("prefix1"), namedKeyword("prefix2"))
        prefix = Predicate.prefix(prefixTerms, suffix)
    }

    @Test
    fun `is finite`() {
        assertThat(prefix.isFinite).isTrue()
        assertThat(prefix.toFinite()).isNotNull {
            assertThat(subject).isSame(prefix)
            assertThat(subject.allTerms()).toBe(prefixTerms + suffixTerms)
        }
    }

    @Test
    fun `contains only prefix terms when suffix is empty`() {

        val prefix = Predicate.prefix(prefixTerms)

        assertThat(prefix.suffix.isEmpty).isTrue()
        assertThat(prefix.toFinite()).isNotNull {
            assertThat(subject.allTerms()).toBe(prefixTerms)
        }
    }

    @Test
    fun `contains only suffix terms when no prefix terms`() {

        val prefix = Predicate.prefix(emptyList(), Predicate.call(suffixTerms))

        assertThat(prefix.terms).isEmpty()
        assertThat(prefix.toFinite()).isNotNull {
            assertThat(subject.allTerms()).toBe(suffixTerms)
        }
    }

    @Test
    fun `has length including prefix and suffix terms`() {
        assertThat(prefix.length()).toBe(prefixTerms.size + suffixTerms.size)
    }

    @Test
    fun `return itself when prefix with the same length requested`() {
        assertThat(prefix.prefix(prefixTerms.size).get()).isSame(prefix)
    }

    @Test
    fun `fails on invalid prefix request`() {
        expect { prefix.prefix(-1) }
                .toThrow<IllegalArgumentException>()
    }

    @Test
    fun `can not build too long prefix`() {
        assertThat(prefix.prefix(prefix.length() + 1)).toBe(Optional.empty())
    }

    @Test
    fun `builds full prefix`() {
        assertThat(prefix.prefix(prefix.length()).get()) {
            assertThat(subject.terms).toBe(prefixTerms + suffixTerms)
            assertThat(subject.suffix.isEmpty).isTrue()
        }
    }

    @Test
    fun `builds shorter prefix`() {
        assertThat(prefix.prefix(1).get()) {
            assertThat(subject.terms).toBe(prefixTerms.slice(0..0))
            assertThat(subject.suffix.length()).toBe(prefix.length() - 1)
            assertThat(subject.suffix.prefix(prefix.length() - 1).get()) {
                assertThat(subject.terms).toBe(prefixTerms.slice(1..(prefixTerms.size - 1)) + suffixTerms)
                assertThat(subject.suffix.isEmpty).isTrue()
            }
        }
    }

    @Test
    fun `builds longer prefix`() {
        assertThat(prefix.prefix(prefixTerms.size + 1).get()) {
            assertThat(subject.terms).toBe(prefixTerms + suffixTerms.slice(0..0))
            assertThat(subject.suffix.length()).toBe(suffix.length() - 1)
            assertThat(subject.suffix.prefix(suffix.length() - 1).get()) {
                assertThat(subject.terms).toBe(suffixTerms.slice(1..(suffixTerms.size - 1)))
                assertThat(subject.suffix.isEmpty).isTrue()
            }
        }
    }

}

class InfinitePrefixTest {

    lateinit var suffixPrefix: IntFunction<Optional<Predicate.Prefix>>
    lateinit var suffix: Predicate.Call
    lateinit var prefixTerms: List<PlainTerm>
    lateinit var prefix: Predicate.Prefix

    @BeforeEach
    fun create() {
        suffixPrefix = mockk("suffixPrefix")
        suffix = Predicate.infiniteCall(suffixPrefix)
        prefixTerms = listOf(namedKeyword("prefix1"), namedKeyword("prefix2"))
        prefix = Predicate.prefix(prefixTerms, suffix)
    }

    @Test
    fun `is infinite`() {
        assertThat(prefix.isFinite).isFalse()
        assertThat(prefix.length()).isLessThan(0)
        assertThat(prefix.toFinite()).isNull()
    }

    @Test
    fun `return itself when prefix with the same length requested`() {
        assertThat(prefix.prefix(prefixTerms.size).get()).isSame(prefix)
    }

    @Test
    fun `fails on invalid prefix request`() {
        expect { prefix.prefix(-1) }
                .toThrow<IllegalArgumentException>()
    }

    @Test
    fun `builds shorter prefix`() {
        assertThat(prefix.prefix(1).get()) {
            assertThat(subject.terms).toBe(prefixTerms.slice(0..0))
            assertThat(subject.suffix.isFinite).isFalse()
            assertThat(subject.suffix.prefix(prefixTerms.size - 1).get())
                    .toBe(Predicate.prefix(prefixTerms.slice(1..(prefixTerms.size - 1)), suffix))
        }
    }

    @Test
    fun `builds longer prefix with infinite suffix`() {

        val suffixTerms = listOf(namedKeyword("suffix1"))
        val suffixSuffix = Predicate.infiniteCall(mockk("suffixSuffix"))

        every { suffixPrefix.apply(1) }.returns(Optional.of(Predicate.prefix(suffixTerms, suffixSuffix)))

        assertThat(prefix.prefix(prefixTerms.size + 1).get()) {
            assertThat(subject.terms).toBe(prefixTerms + suffixTerms)
            assertThat(subject.suffix.isFinite).isFalse()
        }
    }

    @Test
    fun `builds longer prefix with finite suffix`() {

        val suffixTerms = listOf(namedKeyword("suffix1"), namedKeyword("suffix2"))

        every { suffixPrefix.apply(1) }.returns(Optional.of(Predicate.prefix(
                suffixTerms.slice(0..0),
                Predicate.call(suffixTerms.slice(1..(suffixTerms.size - 1))))))

        assertThat(prefix.prefix(prefixTerms.size + 1).get()) {
            assertThat(subject.terms).toBe(prefixTerms + suffixTerms.slice(0..0))
            assertThat(subject.suffix.length()).toBe(suffixTerms.size - 1)
            assertThat(subject.suffix.prefix(suffixTerms.size - 1).get()) {
                assertThat(subject.terms).toBe(suffixTerms.slice(1..(suffixTerms.size - 1)))
                assertThat(subject.suffix.isEmpty).isTrue()
            }
        }
    }

}
