package org.predicode.predicator

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class AtomTest {

    lateinit var knowns: Knowns
    lateinit var resolver: PredicateResolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assert(namedAtom("atom").toString())
                .toBe("'atom'")
    }

    @Test
    fun `matches atom with the same name`() {
        assert(namedAtom("name1").match(namedAtom("name1"), knowns)).isNotNull {
            toBe(knowns)
        }
    }

    @Test
    fun `does not match atom with another name`() {
        assert(namedAtom("name1").match(namedAtom("name2"), knowns))
                .isNull()
    }

    @Test
    fun `resolves variable`() {

        val atom = namedAtom("name")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assert(atom.match(variable, knowns)).isNotNull {
            assert(subject.resolution(variable))
                    .toBe(Knowns.Resolution.Resolved(atom))
        }
    }

    @Test
    fun `does not match other terms`() {

        val atom = namedAtom("name")

        assert(atom.match(namedKeyword("name"), knowns))
                .isNull()
        assert(atom.match(rawValue(123), knowns))
                .isNull()

    }

    @Test
    fun `expands to itself`() {

        val atom = namedAtom("name")

        assert(atom.expand(resolver))
                .toBe(Term.Expansion(atom, knowns))
    }

}
