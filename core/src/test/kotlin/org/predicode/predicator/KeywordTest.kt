package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class KeywordTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `matches keyword with the same name`() {
        assert(namedKeyword("name1").match(namedKeyword("name1"), knowns)).isNotNull {
            toBe(knowns)
        }
    }

    @Test
    fun `does not match keyword with another name`() {
        assert(namedKeyword("name1").match(namedKeyword("name2"), knowns))
                .isNull()
    }

    @Test
    fun `does not match other terms`() {

        val keyword = namedKeyword("name")

        assert(keyword.match(namedAtom("name"), knowns))
                .isNull()
        assert(keyword.match(rawValue(123), knowns))
                .isNull()
        assert(keyword.match(namedVariable("name"), knowns))
                .isNull()
    }

    @Test
    fun `expands to itself`() {

        val keyword = namedKeyword("name")

        assert(keyword.expand(resolver))
                .toBe(Term.Expansion(keyword))
    }

}
