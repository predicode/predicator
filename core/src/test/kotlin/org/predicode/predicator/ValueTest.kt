package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class ValueTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `matches the same value`() {
        assert(rawValue("value1").match(rawValue("value1"), knowns)).isNotNull {
            toBe(knowns)
        }
    }

    @Test
    fun `does not match another value`() {
        assert(rawValue("value1").match(rawValue(123), knowns))
                .isNull()
    }

    @Test
    fun `resolves variable`() {

        val value = rawValue("name")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assert(value.match(variable, knowns)).isNotNull {
            assert(subject.resolution(variable))
                    .toBe(Knowns.Resolution.Resolved(value))
        }
    }

    @Test
    fun `does not match other terms`() {

        val value = rawValue("name")

        assert(value.match(namedKeyword("name"), knowns))
                .isNull()
        assert(value.match(namedAtom("name"), knowns))
                .isNull()

    }

    @Test
    fun `expands to itself`() {

        val value = rawValue("name")

        assert(value.expand(resolver))
                .toBe(Term.Expansion(value, knowns))
    }

}
