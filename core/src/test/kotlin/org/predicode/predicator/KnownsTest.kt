package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assert
import ch.tutteli.atrium.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class KnownsTest {

    @Nested
    inner class Resolution {

        @Test
        fun `fails to resolve unknown variable`() {

            val knowns = Knowns()

            expect { knowns.resolve(namedVariable("unknown"), namedAtom("resolution")) }
                    .toThrow<NoSuchElementException>()
        }

        @Test
        fun `resolves variable`() {

            val variable = namedVariable("variable")
            val knowns = Knowns(variable)
            val resolution = namedAtom("resolution")

            assert(knowns.resolve(variable, resolution)).isNotNull {
                assert(subject.resolution(variable))
                        .toBe(Knowns.Resolution.Resolved(resolution))
            }
        }

        @Test
        fun `does not re-resolve variable to the same resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution)!!

            assert(knowns.resolve(variable, resolution)).isNotNull {
                assert(subject.resolution(variable))
                        .toBe(Knowns.Resolution.Resolved(resolution))
                assert(subject).isSame(knowns)
            }
        }

        @Test
        fun `does not update existing resolution`() {

            val variable = namedVariable("variable")
            val resolution = namedAtom("resolution")
            val knowns = Knowns(variable).resolve(variable, resolution)!!

            assert(knowns.resolve(variable, namedAtom("other")))
                    .isNull()
        }

        @Test
        fun `aliases already resolved variable`() {

            val localVar = namedVariable("localVar")
            val queryVar1 = namedVariable("queryVar1")
            val queryVar2 = namedVariable("queryVar2")
            val resolution = namedAtom("resolution")

            var knowns = Knowns(queryVar1, queryVar2)
                    .map(localVar, queryVar1)!!

            knowns = localVar.match(queryVar2, knowns)!! // Alias queryVar1 -> queryVar2

            assert(knowns.resolve(queryVar1, resolution)).isNotNull {
                assert(subject.resolution(queryVar1))
                        .toBe(Knowns.Resolution.Alias(queryVar2))
                assert(subject.resolution(queryVar2))
                        .toBe(Knowns.Resolution.Resolved(resolution))
                assert(subject.mapping(localVar))
                        .toBe(queryVar1)
            }
        }

    }

}
