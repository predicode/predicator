package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.isSame
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.api.cc.en_UK.toThrow
import ch.tutteli.atrium.assert
import ch.tutteli.atrium.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.namedAtom
import org.predicode.predicator.terms.namedVariable
import java.util.*


class KnownsTest {

    @Nested
    inner class Resolution {

        @Test
        fun `fails to resolve unknown variable`() {

            val knowns = Knowns()

            expect { knowns.resolve(
                    namedVariable("unknown"),
                    namedAtom("resolution")) }
                    .toThrow<UnknownVariableException>()
        }

        @Test
        fun `resolves variable`() {

            val variable = namedVariable("variable")
            val knowns = Knowns(variable)
            val resolution = namedAtom("resolution")

            assert(knowns.resolve(variable, resolution).get()) {
                assert(subject.resolution(variable).value().get())
                        .toBe(resolution)
            }
        }

        @Test
        fun `does not re-resolve variable to the same resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assert(knowns.resolve(variable, resolution).get()) {
                assert(subject.resolution(variable).value().get())
                        .toBe(resolution)
                assert(subject).isSame(knowns)
            }
        }

        @Test
        fun `does not update existing resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution).get()

            assert(knowns.resolve(variable, namedAtom("other")))
                    .toBe(Optional.empty())
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

            assert(knowns.resolve(queryVar1, resolution).get()) {
                assert(subject.resolution(queryVar1).aliased())
                        .toBe(Optional.of(queryVar2))
                assert(subject.resolution(queryVar2).value().get())
                        .toBe(resolution)
                subject.mapping(localVar) { mapping, _ ->
                    assert(mapping)
                            .toBe(queryVar1)
                }
            }
        }

    }

}
