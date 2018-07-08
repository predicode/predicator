package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.Test
import org.predicode.predicator.terms.*
import org.predicode.predicator.terms.Keyword.infix
import org.predicode.predicator.terms.Keyword.prefix


internal class TermPrinterTest {

    @Test
    fun keyword() {
        assertThat(printTerms(Keyword.named("keyword name")))
                .toBe("keyword name")
    }

    @Test
    fun atom() {
        assertThat(printTerms(Atom.named("atom name")))
                .toBe("'atom name")
    }

    @Test
    fun variable() {
        assertThat(printTerms(Variable.named("variable name")))
                .toBe("_variable name")
    }

    @Test
    fun phrase() {
        assertThat(
                printTerms(
                        Phrase {
                            k("keyword")
                            a("atom")
                        }))
                .toBe("(keyword 'atom)")
    }

    @Test
    fun `closes quotes before keywords`() {
        assertThat(printTerms(
                Atom.named("atom"),
                Keyword.named("keyword")))
                .toBe("'atom' keyword")
        assertThat(
                printTerms(
                        Variable.named("variable"),
                        Keyword.named("keyword")))
                .toBe("_variable_ keyword")
    }

    @Test
    fun `appends infix operators`() {
        assertThat(printTerms(
                Atom.named("atom"),
                infix("op")))
                .toBe("'atom'op")
        assertThat(
                printTerms(
                        Variable.named("variable"),
                        infix("op")))
                .toBe("_variable_op")
    }

    @Test
    fun `separates keywords`() {
        assertThat(printTerms(
                Keyword.named("first"),
                Keyword.named("second")))
                .toBe("first second")
    }

    @Test
    fun `separates keyword and operator`() {
        assertThat(printTerms(
                Keyword.named("first"),
                infix("second")))
                .toBe("first`second")
        assertThat(printTerms(
                prefix("first"),
                Keyword.named("second")))
                .toBe("first`second")
        assertThat(printTerms(
                prefix("first"),
                infix("second")))
                .toBe("first`second")
    }

    @Test
    fun `after placeholder`() {
        assertThat(printTerms(
                Placeholder.placeholder(),
                Atom.named("second")))
                .toBe("_ 'second")
        assertThat(printTerms(
                Placeholder.placeholder(),
                Variable.named("second")))
                .toBe("_ _second")
        assertThat(printTerms(
                Placeholder.placeholder(),
                Value.raw("second")))
                .toBe("_ [second]")
        assertThat(printTerms(
                Placeholder.placeholder(),
                Placeholder.placeholder()))
                .toBe("_ _")
        assertThat(printTerms(
                Placeholder.placeholder(),
                Phrase(Keyword.named("second"))))
                .toBe("_ (second)")
    }

    @Test
    fun `before placeholder`() {
        assertThat(printTerms(
                Atom.named("first"),
                Placeholder.placeholder()))
                .toBe("'first _")
        assertThat(printTerms(
                Variable.named("first"),
                Placeholder.placeholder()))
                .toBe("_first _")
        assertThat(printTerms(
                Value.raw("first"),
                Placeholder.placeholder()))
                .toBe("[first] _")
        assertThat(printTerms(
                Phrase(Keyword.named("first")),
                Placeholder.placeholder()))
                .toBe("(first) _")
    }

    @Test
    fun `prefix operator`() {
        assertThat(printTerms(
                prefix("first"),
                Atom.named("second")))
                .toBe("first'second")
        assertThat(printTerms(
                prefix("first"),
                Variable.named("second")))
                .toBe("first_second")
        assertThat(printTerms(
                prefix("first"),
                Value.raw("second")))
                .toBe("first[second]")
        assertThat(printTerms(
                prefix("first"),
                Placeholder.placeholder()))
                .toBe("first`_")
        assertThat(printTerms(
                prefix("first"),
                Phrase(Keyword.named("second"))))
                .toBe("first(second)")
    }

    @Test
    fun `infix operator`() {
        assertThat(printTerms(
                Atom.named("first"),
                infix("second")))
                .toBe("'first'second")
        assertThat(printTerms(
                Variable.named("first"),
                infix("second")))
                .toBe("_first_second")
        assertThat(printTerms(
                Value.raw("first"),
                infix("second")))
                .toBe("[first]second")
        assertThat(printTerms(
                Placeholder.placeholder(),
                infix("second")))
                .toBe("_`second")
        assertThat(printTerms(
                Phrase(Keyword.named("first")),
                infix("second")))
                .toBe("(first)second")
    }

    @Test
    fun `separates keywords from quoted terms`() {
        assertThat(printTerms(
                Keyword.named("keyword"),
                Atom.named("atom")))
                .toBe("keyword 'atom")
        assertThat(printTerms(
                Atom.named("atom"),
                Keyword.named("keyword")))
                .toBe("'atom' keyword")
        assertThat(printTerms(
                Keyword.named("keyword"),
                Variable.named("variable")))
                .toBe("keyword _variable")
        assertThat(printTerms(
                Variable.named("variable"),
                Keyword.named("keyword")))
                .toBe("_variable_ keyword")
    }

    @Test
    fun `does not double-quote terms`() {
        assertThat(printTerms(
                Atom.named("atom+"),
                Keyword.named("keyword")))
                .toBe("'atom+' keyword")
        assertThat(printTerms(
                Variable.named("variable+"),
                Keyword.named("keyword")))
                .toBe("_variable+_ keyword")
    }

    @Test
    fun `separates values from quoted terms`() {
        assertThat(printTerms(
                Value.raw("value"),
                Atom.named("atom")))
                .toBe("[value] 'atom")
        assertThat(printTerms(
                Atom.named("atom"),
                Value.raw("value")))
                .toBe("'atom [value]")
        assertThat(printTerms(
                Value.raw("value"),
                Variable.named("variable")))
                .toBe("[value] _variable")
        assertThat(printTerms(
                Variable.named("variable"),
                Value.raw("value")))
                .toBe("_variable [value]")
    }

    @Test
    fun `separates phrases from quoted terms`() {

        val phrase = Phrase {
            k("keyword")
            raw("value")
        }

        assertThat(printTerms(phrase, Atom.named("atom")))
                .toBe("(keyword [value]) 'atom")
        assertThat(printTerms(Atom.named("atom"), phrase))
                .toBe("'atom (keyword [value])")
        assertThat(printTerms(phrase, Variable.named("variable")))
                .toBe("(keyword [value]) _variable")
        assertThat(printTerms(Variable.named("variable"), phrase))
                .toBe("_variable (keyword [value])")
    }

    @Test
    fun `separates values from keywords`() {
        assertThat(printTerms(
                Value.raw("value"),
                Keyword.named("keyword")))
                .toBe("[value] keyword")
        assertThat(printTerms(
                Keyword.named("keyword"),
                Value.raw("value")))
                .toBe("keyword [value]")
    }

    @Test
    fun `separates phrases from keywords`() {

        val phrase = Phrase {
            k("keyword")
            raw("value")
        }

        assertThat(printTerms(phrase, Keyword.named("keyword")))
                .toBe("(keyword [value]) keyword")
        assertThat(printTerms(Keyword.named("keyword"), phrase))
                .toBe("keyword (keyword [value])")
    }

    @Test
    fun `separates values`() {
        assertThat(printTerms(
                Value.raw("first"),
                Value.raw("second")))
                .toBe("[first] [second]")
    }

    @Test
    fun `separates phrases`() {
        assertThat(
                printTerms(
                        Phrase { k("phrase 1") },
                        Phrase { k("phrase 2") }))
                .toBe("(phrase 1) (phrase 2)")
    }

}
