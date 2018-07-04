package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.Knowns
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.TestPredicateResolver
import org.predicode.predicator.testutils.isEmpty
import org.predicode.predicator.testutils.notToBeEmpty
import org.predicode.predicator.testutils.toContain


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
        assertThat(rawValue("value1").match(rawValue("value1"), knowns)).toContain(knowns)
    }

    @Test
    fun `does not match another value`() {
        assertThat(rawValue("value1").match(rawValue(123), knowns)).isEmpty()
    }

    @Test
    fun `resolves variable`() {

        val value = rawValue("value")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assertThat(value.match(variable, knowns)).notToBeEmpty {
            assertThat(subject.resolution(variable).value()).toContain(value)
        }
    }

    @Test
    fun `matches placeholder`() {
        assertThat(rawValue("value").match(Placeholder.placeholder(), knowns))
                .toContain(knowns)
    }

    @Test
    fun `does not match other terms`() {

        val value = rawValue("name")

        assertThat(value.match(namedKeyword("name"), knowns)).isEmpty()
        assertThat(value.match(namedAtom("name"), knowns)).isEmpty()
    }

    @Test
    fun `expands to itself`() {

        val value = rawValue("name")

        assertThat(value.expand(resolver))
                .toContain(Term.Expansion(value, knowns))
    }

}
