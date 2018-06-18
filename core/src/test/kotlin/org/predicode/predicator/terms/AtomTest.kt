package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.*
import java.util.*


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
        assert(namedAtom("name1").match(namedAtom("name1"), knowns))
                .toBe(Optional.of(knowns))
    }

    @Test
    fun `matches placeholder`() {
        assert(namedAtom("name1").match(Placeholder.placeholder(), knowns))
                .toBe(Optional.of(knowns))
    }

    @Test
    fun `does not match atom with another name`() {
        assert(namedAtom("name1").match(namedAtom("name2"), knowns))
                .toBe(Optional.empty())
    }

    @Test
    fun `resolves variable`() {

        val atom = namedAtom("name")
        val variable = namedVariable("var")

        knowns = Knowns(variable)

        assert(atom.match(variable, knowns).get()) {
            assert(subject.resolution(variable).value().get())
                    .toBe(atom)
        }
    }

    @Test
    fun `does not match other terms`() {

        val atom = namedAtom("name")

        assert(atom.match(namedKeyword("name"), knowns))
                .toBe(Optional.empty())
        assert(atom.match(rawValue(123), knowns))
                .toBe(Optional.empty())

    }

    @Test
    fun `expands to itself`() {

        val atom = namedAtom("name")

        assert(atom.expand(resolver).get())
                .toBe(Term.Expansion(atom, knowns))
    }

}
