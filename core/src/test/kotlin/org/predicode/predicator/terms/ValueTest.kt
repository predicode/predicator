package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.Knowns
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.TestPredicateResolver
import java.util.*


class ValueTest {

    lateinit var knowns: Knowns
    lateinit var resolver: Predicate.Resolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assertThat(namedVariable("variable").toString())
                .toBe("_variable_")
    }

    @Test
    fun `matches the same value`() {
        assertThat(rawValue("value1").match(rawValue("value1"), knowns).get())
                .toBe(knowns)
    }

    @Test
    fun `does not match another value`() {
        assertThat(rawValue("value1").match(rawValue(123), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `resolves variable`() {

        val value = rawValue("value")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assertThat(value.match(variable, knowns).get()) {
            assertThat(subject.resolution(variable).value().get())
                    .toBe(value)
        }
    }

    @Test
    fun `matches placeholder`() {
        assertThat(rawValue("value").match(Placeholder.placeholder(), knowns))
                .toBe(Optional.of(knowns))
    }

    @Test
    fun `does not match other terms`() {

        val value = rawValue("name")

        assertThat(value.match(namedKeyword("name"), knowns))
                .toBe(Optional.empty())
        assertThat(value.match(namedAtom("name"), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `expands to itself`() {

        val value = rawValue("name")

        assertThat(value.expand(resolver).get())
                .toBe(Term.Expansion(value, knowns))
    }

}
