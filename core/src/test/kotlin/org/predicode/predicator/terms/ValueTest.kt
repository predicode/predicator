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
        knowns = Knowns.none()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assertThat(Variable.named("variable").toString())
                .toBe("_variable_")
    }

    @Test
    fun `matches the same value`() {
        assertThat(Value.raw("value1").match(Value.raw("value1"), knowns)).toContain(knowns)
    }

    @Test
    fun `does not match another value`() {
        assertThat(Value.raw("value1").match(Value.raw(123), knowns)).isEmpty()
    }

    @Test
    fun `resolves variable`() {

        val value = Value.raw("value")
        val variable = Variable.named("var")

        knowns = Knowns.forVariables(variable)

        assertThat(value.match(variable, knowns)).notToBeEmpty {
            assertThat(subject.resolution(variable).value()).toContain(value)
        }
    }

    @Test
    fun `matches placeholder`() {
        assertThat(Value.raw("value").match(Placeholder.placeholder(), knowns))
                .toContain(knowns)
    }

    @Test
    fun `does not match other terms`() {

        val value = Value.raw("name")

        assertThat(value.match(Keyword.named("name"), knowns)).isEmpty()
        assertThat(value.match(Atom.named("name"), knowns)).isEmpty()
    }

    @Test
    fun `expands to itself`() {

        val value = Value.raw("name")

        assertThat(value.expand(resolver))
                .toContain(Term.Expansion(value, knowns))
    }

}
