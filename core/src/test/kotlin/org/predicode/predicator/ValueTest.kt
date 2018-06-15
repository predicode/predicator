package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*


class ValueTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assert(namedVariable("variable").toString())
                .toBe("_variable_")
    }

    @Test
    fun `matches the same value`() {
        assert(rawValue("value1").match(rawValue("value1"), knowns).get())
                .toBe(knowns)
    }

    @Test
    fun `does not match another value`() {
        assert(rawValue("value1").match(rawValue(123), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `resolves variable`() {

        val value = rawValue("name")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assert(value.match(variable, knowns).get()) {
            assert(subject.resolution(variable).value().get())
                    .toBe(value)
        }
    }

    @Test
    fun `does not match other terms`() {

        val value = rawValue("name")

        assert(value.match(namedKeyword("name"), knowns))
                .toBe(Optional.empty())
        assert(value.match(namedAtom("name"), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `expands to itself`() {

        val value = rawValue("name")

        assert(value.expand(resolver).get())
                .toBe(Term.Expansion(value, knowns))
    }

}
