package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.*

class QualifierTest {

    @Test
    fun signature() {

        val qualifier = testQualifier()

        assertThat(qualifier.signature).toBe(
                Qualifier.signature(Keyword.namedKeyword("qualifier1"),
                        Placeholder.placeholder(),
                        Keyword.namedKeyword("keyword2"),
                        Placeholder.placeholder(),
                        Placeholder.placeholder(),
                        Placeholder.placeholder()))
        assertThat(qualifier.signature).toBe(testQualifier1().signature)
    }

}

fun testQualifier(): Qualifier = Qualifier.qualifier(
        Keyword.namedKeyword("qualifier1"),
        Atom.namedAtom("atom"),
        Keyword.namedKeyword("keyword2"),
        Variable.namedVariable("variable"),
        Placeholder.placeholder(),
        Value.rawValue(123))

fun testQualifier1(): Qualifier = Qualifier.qualifier(
        Keyword.namedKeyword("qualifier1"),
        Variable.namedVariable("variable"),
        Keyword.namedKeyword("keyword2"),
        Atom.namedAtom("atom"),
        Placeholder.placeholder(),
        Value.rawValue("abc"))

fun testQualifier2(): Qualifier = Qualifier.qualifier(
        Atom.namedAtom("qualifier2"),
        Keyword.namedKeyword("keyword3"),
        Value.rawValue(321))
