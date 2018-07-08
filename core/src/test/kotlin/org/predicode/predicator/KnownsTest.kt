package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_GB.isSameAs
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assertThat
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.Atom
import org.predicode.predicator.terms.Variable
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
                    Variable.named("unknown"),
                    Atom.named("resolution")) }
                    .toThrow<UnknownVariableException> {}
        }

        @Test
        fun `resolves variable`() {

            val variable = Variable.named("variable")
            val knowns = Knowns(variable)
            val resolution = Atom.named("resolution")

            assertThat(knowns.resolve(variable, resolution)).notToBeEmpty {
                assertThat(subject.resolution(variable).value())
                        .toContain(resolution)
            }
        }

        @Test
        fun `does not re-resolve variable to the same resolution`() {

            val variable = Variable.named("variable")
            val resolution = Atom.named("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assertThat(knowns.resolve(variable, resolution)).notToBeEmpty {
                assertThat(subject.resolution(variable).value())
                        .toContain(resolution)
                assertThat(subject).isSameAs(knowns)
            }
        }

        @Test
        fun `does not update existing resolution`() {

            val variable = Variable.named("variable")
            val resolution = Atom.named("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assertThat(knowns.resolve(variable, Atom.named("other"))).isEmpty()
        }

        @Test
        fun `aliases already resolved variable`() {

            val localVar = Variable.named("localVar")
            val queryVar1 = Variable.named("queryVar1")
            val queryVar2 = Variable.named("queryVar2")
            val resolution = Atom.named("resolution")

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
