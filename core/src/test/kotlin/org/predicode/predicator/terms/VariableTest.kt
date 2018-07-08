package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_GB.notToBe
import ch.tutteli.atrium.api.cc.en_GB.startsWith
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assertThat
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.predicode.predicator.Knowns
import org.predicode.predicator.UnknownVariableException
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.TestPredicateResolver
import org.predicode.predicator.testutils.isEmpty
import org.predicode.predicator.testutils.notToBeEmpty
import org.predicode.predicator.testutils.toContain

class VariableTest {

    private lateinit var knowns: Knowns
    private lateinit var resolver: Predicate.Resolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `does not match keyword`() {
        assertThat(
                Variable.named("variable").match(
                        Keyword.named("keyword"), knowns))
                .isEmpty()
    }

    @Test
    fun `maps to atom`() {

        val variable = Variable.named("variable")
        val atom = Atom.named("atom")

        assertThat(variable.match(atom, knowns)).notToBeEmpty {
            subject.mapping(variable) { mapping, _ ->
                assertThat(mapping)
                        .toBe(atom)
            }
        }
    }

    @Test
    fun `maps to value`() {

        val variable = Variable.named("variable")
        val value = Value.raw("value")

        assertThat(variable.match(value, knowns)).notToBeEmpty {
            subject.mapping(variable) { mapping, _ ->
                assertThat(mapping)
                        .toBe(value)
            }
        }
    }

    @Test
    fun `matches placeholder`() {
        assertThat(Variable.named("variable").match(Placeholder.placeholder(), knowns))
                .toContain(knowns)
    }

    @Test
    fun `does not remap the same term`() {

        val variable = Variable.named("variable")
        val value = Value.raw(12345)

        knowns = knowns.map(variable, value).get()

        assertThat(variable.match(value, knowns)).notToBeEmpty {
            subject.mapping(variable) { mapping, _ ->
                assertThat(mapping)
                        .toBe(value)
            }
        }
    }

    @Test
    fun `does not remap to another term`() {

        val variable = Variable.named("variable")

        knowns = knowns.map(variable, Value.raw(12345)).get()

        assertThat(variable.match(Atom.named("atom"), knowns)).isEmpty()
    }

    @Test
    fun `fails to map to unknown query variable`() {

        val localVar = Variable.named("localVar")
        val queryVar = Variable.named("queryVar")

        expect { localVar.match(queryVar, knowns) }
                .toThrow<UnknownVariableException> {}
    }

    @Test
    fun `maps to query variable`() {

        val localVar = Variable.named("localVar")
        val queryVar = Variable.named("queryVar")

        knowns = Knowns(queryVar)

        assertThat(localVar.match(queryVar, knowns)).notToBeEmpty {
            subject.mapping(localVar) { mapping, _ ->
                assertThat(mapping)
                        .toBe(queryVar)
            }
        }
    }

    @Test
    fun `resolves already mapped query variable`() {

        val localVar = Variable.named("localVar")
        val queryVar = Variable.named("queryVar")

        knowns = Knowns(queryVar)
                .map(localVar, queryVar)
                .get()

        val resolution = Atom.named("resolution")

        assertThat(localVar.match(resolution, knowns)).notToBeEmpty {
            assertThat(subject.resolution(queryVar).value())
                    .toContain(resolution)
            subject.mapping(localVar) { mapping, _ ->
                assertThat(mapping)
                        .toBe(queryVar)
            }
        }
    }

    @Test
    fun `does not re-resolve mapped query variable to the same term`() {

        val localVar = Variable.named("localVar")
        val queryVar = Variable.named("queryVar")
        val resolution = Atom.named("resolution")

        knowns = Knowns(queryVar)
                .resolve(queryVar, resolution).get()
                .map(localVar, queryVar).get()

        assertThat(localVar.match(resolution, knowns)).notToBeEmpty {
            assertThat(subject.resolution(queryVar).value())
                    .toContain(resolution)
            subject.mapping(localVar) { mapping, _ ->
                assertThat(mapping)
                        .toBe(queryVar)
            }
        }
    }

    @Test
    fun `does not resolve mapped query variable to another term`() {

        val localVar = Variable.named("localVar")
        val queryVar = Variable.named("queryVar")

        knowns = Knowns(queryVar)
                .resolve(queryVar, Atom.named("resolution")).get()
                .map(localVar, queryVar).get()

        assertThat(localVar.match(Atom.named("other resolution"), knowns)).isEmpty()
    }

    @Test
    fun `aliases already mapped query variable`() {

        val localVar = Variable.named("localVar")
        val queryVar1 = Variable.named("queryVar1")
        val queryVar2 = Variable.named("queryVar2")

        knowns = Knowns(queryVar1, queryVar2)
                .map(localVar, queryVar1).get()

        assertThat(localVar.match(queryVar2, knowns)).notToBeEmpty {
            assertThat(subject.resolution(queryVar1).aliased())
                    .toContain(queryVar2)
            subject.mapping(localVar) { mapping, _ ->
                assertThat(mapping)
                        .toBe(queryVar1)
            }
        }
    }

    @Test
    fun `deep aliases already mapped query variable`() {

        val localVar = Variable.named("localVar")
        val queryVar1 = Variable.named("queryVar1")
        val queryVar2 = Variable.named("queryVar2")
        val queryVar3 = Variable.named("queryVar3")

        knowns = Knowns(queryVar1, queryVar2, queryVar3)
                .map(localVar, queryVar1).get()
        knowns = localVar.match(queryVar2, knowns).get()

        assertThat(localVar.match(queryVar3, knowns)).notToBeEmpty {
            assertThat(subject.resolution(queryVar1).aliased())
                    .toContain(queryVar2)
            assertThat(subject.resolution(queryVar2).aliased())
                    .toContain(queryVar3)
            subject.mapping(localVar) { mapping, _ ->
                assertThat(mapping)
                        .toBe(queryVar1)
            }
        }
    }

    @Test
    fun `expands to mapping`() {

        val variable = Variable.named("variable")
        val value = Value.raw(12345)

        knowns = knowns.map(variable, value).get()
        resolver = resolver.withKnowns(knowns)

        assertThat(variable.expand(resolver))
                .toContain(Term.Expansion(value, knowns))
    }

    @Nested
    @DisplayName("Temporary variable")
    inner class TempVariableTests {

        @Test
        fun `compared by identity`() {

            val variable = Variable.temp("t")

            assertThat(variable).toBe(variable)
            assertThat(variable).notToBe(Variable.temp("t"))
        }

        @Test
        fun `has a name with the given prefix`() {

            val prefix = "temp variable prefix"
            val variable = Variable.temp(prefix)

            assertThat(variable.name)
                    .startsWith("$prefix ")
        }

    }

}
