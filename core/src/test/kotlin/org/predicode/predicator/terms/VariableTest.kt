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
                namedVariable("variable").match(
                        namedKeyword(
                                "keyword"), knowns))
                .isEmpty()
    }

    @Test
    fun `maps to atom`() {

        val variable = namedVariable("variable")
        val atom = namedAtom("atom")

        assertThat(variable.match(atom, knowns)).notToBeEmpty {
            subject.mapping(variable) { mapping, _ ->
                assertThat(mapping)
                        .toBe(atom)
            }
        }
    }

    @Test
    fun `maps to value`() {

        val variable = namedVariable("variable")
        val value = rawValue("value")

        assertThat(variable.match(value, knowns)).notToBeEmpty {
            subject.mapping(variable) { mapping, _ ->
                assertThat(mapping)
                        .toBe(value)
            }
        }
    }

    @Test
    fun `matches placeholder`() {
        assertThat(namedVariable("variable").match(Placeholder.placeholder(), knowns))
                .toContain(knowns)
    }

    @Test
    fun `does not remap the same term`() {

        val variable = namedVariable("variable")
        val value = rawValue(12345)

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

        val variable = namedVariable("variable")

        knowns = knowns.map(variable, rawValue(12345)).get()

        assertThat(variable.match(namedAtom("atom"), knowns)).isEmpty()
    }

    @Test
    fun `fails to map to unknown query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        expect { localVar.match(queryVar, knowns) }
                .toThrow<UnknownVariableException> {}
    }

    @Test
    fun `maps to query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

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

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        knowns = Knowns(queryVar)
                .map(localVar, queryVar)
                .get()

        val resolution = namedAtom("resolution")

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

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")
        val resolution = namedAtom("resolution")

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

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        knowns = Knowns(queryVar)
                .resolve(queryVar, namedAtom("resolution")).get()
                .map(localVar, queryVar).get()

        assertThat(localVar.match(namedAtom("other resolution"), knowns)).isEmpty()
    }

    @Test
    fun `aliases already mapped query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar1 = namedVariable("queryVar1")
        val queryVar2 = namedVariable("queryVar2")

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

        val localVar = namedVariable("localVar")
        val queryVar1 = namedVariable("queryVar1")
        val queryVar2 = namedVariable("queryVar2")
        val queryVar3 = namedVariable("queryVar3")

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

        val variable = namedVariable("variable")
        val value = rawValue(12345)

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

            val variable = tempVariable("t")

            assertThat(variable).toBe(variable)
            assertThat(variable).notToBe(tempVariable("t"))
        }

        @Test
        fun `has a name with the given prefix`() {

            val prefix = "temp variable prefix"
            val variable = tempVariable(prefix)

            assertThat(variable.name)
                    .startsWith("$prefix ")
        }

    }

}
