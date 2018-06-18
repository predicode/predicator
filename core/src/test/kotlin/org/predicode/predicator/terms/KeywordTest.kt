package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.*
import java.util.*


class KeywordTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assert(namedKeyword("keyword").toString())
                .toBe("`keyword`")
    }

    @Test
    fun `matches keyword with the same name`() {
        assert(namedKeyword("name1").match(namedKeyword("name1"), knowns).get()) {
            toBe(knowns)
        }
    }

    @Test
    fun `does not match keyword with another name`() {
        assert(namedKeyword("name1").match(namedKeyword("name2"), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `does not match placeholder`() {
        assert(namedKeyword("name").match(Placeholder.placeholder(), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `does not match other terms`() {

        val keyword = namedKeyword("name")

        assert(keyword.match(namedAtom("name"), knowns))
                .toBe(Optional.empty())
        assert(keyword.match(rawValue(123), knowns))
                .toBe(Optional.empty())
        assert(keyword.match(namedVariable("name"), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `expands to itself`() {

        val keyword = namedKeyword("name")

        assert(keyword.expand(resolver).get())
                .toBe(Term.Expansion(keyword, knowns))
    }

}
