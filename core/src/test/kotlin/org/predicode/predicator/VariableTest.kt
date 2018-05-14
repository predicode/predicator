package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.api.cc.en_UK.toThrow
import ch.tutteli.atrium.assert
import ch.tutteli.atrium.expect
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VariableTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `does not match keyword`() {
        assert(namedVariable("variable").match(namedKeyword("keyword"), knowns))
                .isNull()
    }

    @Test
    fun `maps to atom`() {

        val variable = namedVariable("variable")
        val atom = namedAtom("atom")

        assert(variable.match(atom, knowns)).isNotNull {
            assert(subject.mapping(variable))
                    .toBe(atom)
        }
    }

    @Test
    fun `maps to value`() {

        val variable = namedVariable("variable")
        val value = simpleValue("value")

        assert(variable.match(value, knowns)).isNotNull {
            assert(subject.mapping(variable))
                    .toBe(value)
        }
    }

    @Test
    fun `does not remap the same term`() {

        val variable = namedVariable("variable")
        val value = simpleValue(12345)

        knowns = knowns.map(variable, value)!!

        assert(variable.match(value, knowns)).isNotNull {
            assert(subject.mapping(variable))
                    .toBe(value)
        }
    }

    @Test
    fun `does not remap to another term`() {

        val variable = namedVariable("variable")

        knowns = knowns.map(variable, simpleValue(12345))!!

        assert(variable.match(namedAtom("atom"), knowns))
                .isNull()
    }

    @Test
    fun `fails to map to unknown query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        expect { localVar.match(queryVar, knowns) }
                .toThrow<NoSuchElementException>()
    }

    @Test
    fun `maps to query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        knowns = Knowns(queryVar)

        assert(localVar.match(queryVar, knowns)).isNotNull {
            assert(subject.mapping(localVar))
                    .toBe(queryVar)
        }
    }

    @Test
    fun `resolves already mapped query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        knowns = Knowns(queryVar)
                .map(localVar, queryVar)!!

        val resolution = namedAtom("resolution")

        assert(localVar.match(resolution, knowns)).isNotNull {
            assert(subject.resolution(queryVar))
                    .toBe(Knowns.Resolution.Resolved(resolution))
            assert(subject.mapping(localVar))
                    .toBe(queryVar)
        }
    }

    @Test
    fun `does not re-resolve mapped query variable to the same term`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")
        val resolution = namedAtom("resolution")

        knowns = Knowns(queryVar)
                .resolve(queryVar, resolution)!!
                .map(localVar, queryVar)!!

        assert(localVar.match(resolution, knowns)).isNotNull {
            assert(subject.resolution(queryVar))
                    .toBe(Knowns.Resolution.Resolved(resolution))
            assert(subject.mapping(localVar))
                    .toBe(queryVar)
        }
    }

    @Test
    fun `does not resolve mapped query variable to another term`() {

        val localVar = namedVariable("localVar")
        val queryVar = namedVariable("queryVar")

        knowns = Knowns(queryVar)
                .resolve(queryVar, namedAtom("resolution"))!!
                .map(localVar, queryVar)!!

        assert(localVar.match(namedAtom("other resolution"), knowns))
                .isNull()
    }

    @Test
    fun `aliases already mapped query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar1 = namedVariable("queryVar1")
        val queryVar2 = namedVariable("queryVar2")

        knowns = Knowns(queryVar1, queryVar2)
                .map(localVar, queryVar1)!!

        assert(localVar.match(queryVar2, knowns)).isNotNull {
            assert(subject.resolution(queryVar1))
                    .toBe(Knowns.Resolution.Alias(queryVar2))
            assert(subject.mapping(localVar))
                    .toBe(queryVar1)
        }
    }

    @Test
    fun `deep aliases already mapped query variable`() {

        val localVar = namedVariable("localVar")
        val queryVar1 = namedVariable("queryVar1")
        val queryVar2 = namedVariable("queryVar2")
        val queryVar3 = namedVariable("queryVar3")

        knowns = Knowns(queryVar1, queryVar2, queryVar3)
                .map(localVar, queryVar1)!!
        knowns = localVar.match(queryVar2, knowns)!!

        assert(localVar.match(queryVar3, knowns)).isNotNull {
            assert(subject.resolution(queryVar1))
                    .toBe(Knowns.Resolution.Alias(queryVar2))
            assert(subject.resolution(queryVar2))
                    .toBe(Knowns.Resolution.Alias(queryVar3))
            assert(subject.mapping(localVar))
                    .toBe(queryVar1)
        }
    }

    @Test
    fun `expands to mapping`() {

        val variable = namedVariable("variable")
        val value = simpleValue(12345)

        assert(knowns.map(variable, value)).isNotNull {
            resolver = resolver.withKnowns(subject)
            assert(variable.expand(resolver))
                    .toBe(Term.Expansion(value))
        }
    }

}
