package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_GB.isSameAs
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assertThat
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.namedAtom
import org.predicode.predicator.terms.namedVariable
import org.predicode.predicator.testutils.isEmpty
import org.predicode.predicator.testutils.notToBeEmpty
import org.predicode.predicator.testutils.toContain


class KnownsTest {

    @Nested
    inner class Resolution {

        @Test
        fun `fails to resolve unknown variable`() {

            val knowns = Knowns()

            expect { knowns.resolve(
                    namedVariable("unknown"),
                    namedAtom("resolution")) }
                    .toThrow<UnknownVariableException> {}
        }

        @Test
        fun `resolves variable`() {

            val variable = namedVariable("variable")
            val knowns = Knowns(variable)
            val resolution = namedAtom("resolution")

            assertThat(knowns.resolve(variable, resolution)).notToBeEmpty {
                assertThat(subject.resolution(variable).value())
                        .toContain(resolution)
            }
        }

        @Test
        fun `does not re-resolve variable to the same resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assertThat(knowns.resolve(variable, resolution)).notToBeEmpty {
                assertThat(subject.resolution(variable).value())
                        .toContain(resolution)
                assertThat(subject).isSameAs(knowns)
            }
        }

        @Test
        fun `does not update existing resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assertThat(knowns.resolve(variable, namedAtom("other"))).isEmpty()
        }

        @Test
        fun `aliases already resolved variable`() {

            val localVar = namedVariable("localVar")
            val queryVar1 = namedVariable("queryVar1")
            val queryVar2 = namedVariable("queryVar2")
            val resolution = namedAtom("resolution")

            var knowns = Knowns(queryVar1, queryVar2)
                    .map(localVar, queryVar1).get()

            knowns = localVar.match(queryVar2, knowns).get() // Alias queryVar1 -> queryVar2

            assertThat(knowns.resolve(queryVar1, resolution)).notToBeEmpty {
                assertThat(subject.resolution(queryVar1).aliased())
                        .toContain(queryVar2)
                assertThat(subject.resolution(queryVar2).value())
                        .toContain(resolution)
                subject.mapping(localVar) { mapping, _ ->
                    assertThat(mapping)
                            .toBe(queryVar1)
                }
            }
        }

    }

}
