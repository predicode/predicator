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
                Qualifier.signature(Keyword.named("qualifier1"),
                        Placeholder.placeholder(),
                        Keyword.named("keyword2"),
                        Placeholder.placeholder(),
                        Placeholder.placeholder(),
                        Placeholder.placeholder()))
        assertThat(qualifier.signature).toBe(testQualifier1().signature)
    }

}

fun testQualifier(): Qualifier = Qualifier.qualifier(
        Keyword.named("qualifier1"),
        Atom.named("atom"),
        Keyword.named("keyword2"),
        Variable.named("variable"),
        Placeholder.placeholder(),
        Value.raw(123))

fun testQualifier1(): Qualifier = Qualifier.qualifier(
        Keyword.named("qualifier1"),
        Variable.named("variable"),
        Keyword.named("keyword2"),
        Atom.named("atom"),
        Placeholder.placeholder(),
        Value.raw("abc"))

fun testQualifier2(): Qualifier = Qualifier.qualifier(
        Atom.named("qualifier2"),
        Keyword.named("keyword3"),
        Value.raw(321))
