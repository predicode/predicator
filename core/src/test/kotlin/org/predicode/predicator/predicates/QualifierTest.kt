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
                newQualifierSignature {
                    k("qualifier1")
                    p
                    k("keyword2")
                    p
                    p
                    p
                })
        assertThat(qualifier.signature).toBe(testQualifier1().signature)
    }

}

fun testQualifier(): Qualifier = newQualifier {
    k("qualifier1")
    a("atom")
    k("keyword2")
    v("variable")
    p
    raw(123)
}

fun testQualifier1(): Qualifier = newQualifier {
    k("qualifier1")
    v("variable")
    k("keyword2")
    a("atom")
    p
    raw("abc")
}

fun testQualifier2(): Qualifier = newQualifier {
    a("qualifier2")
    k("keyword3")
    raw(321)
}
