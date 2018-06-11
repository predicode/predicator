package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.Test
import org.predicode.predicator.*


internal class TermPrinterTest {

    @Test
    fun keyword() {
        assert(printTerms(namedKeyword("keyword name")))
                .toBe("keyword name")
    }

    @Test
    fun atom() {
        assert(printTerms(namedAtom("atom name")))
                .toBe("'atom name")
    }

    @Test
    fun variable() {
        assert(printTerms(namedVariable("variable name")))
                .toBe("_variable name")
    }

    @Test
    fun phrase() {
        assert(
                printTerms(
                        Phrase(
                                namedKeyword("keyword"),
                                namedAtom("atom"))))
                .toBe("(keyword 'atom)")
    }

    @Test
    fun `closes quotes before keywords`() {
        ch.tutteli.atrium.assert(printTerms(namedAtom("atom"), namedKeyword("keyword")))
                .toBe("'atom' keyword")
        ch.tutteli.atrium.assert(
                printTerms(
                        namedVariable("variable"),
                        namedKeyword("keyword")))
                .toBe("_variable_ keyword")
    }

    @Test
    fun `separates keywords`() {
        assert(printTerms(namedKeyword("first"), namedKeyword("second")))
                .toBe("first second")
    }

    @Test
    fun `separates values from quoted terms`() {
        assert(printTerms(rawValue("value"), namedAtom("atom")))
                .toBe("[value] 'atom")
        assert(printTerms(namedAtom("atom"), rawValue("value")))
                .toBe("'atom [value]")
        assert(printTerms(rawValue("value"), namedVariable("variable")))
                .toBe("[value] _variable")
        assert(printTerms(namedVariable("variable"), rawValue("value")))
                .toBe("_variable [value]")
    }

    @Test
    fun `separates phrases from quoted terms`() {

        val phrase = Phrase(
                namedKeyword("keyword"),
                rawValue("value"))

        assert(printTerms(phrase, namedAtom("atom")))
                .toBe("(keyword [value]) 'atom")
        assert(printTerms(namedAtom("atom"), phrase))
                .toBe("'atom (keyword [value])")
        assert(printTerms(phrase, namedVariable("variable")))
                .toBe("(keyword [value]) _variable")
        assert(printTerms(namedVariable("variable"), phrase))
                .toBe("_variable (keyword [value])")
    }

    @Test
    fun `separates values from keywords`() {
        assert(printTerms(rawValue("value"), namedKeyword("keyword")))
                .toBe("[value] keyword")
        assert(printTerms(namedKeyword("keyword"), rawValue("value")))
                .toBe("keyword [value]")
    }

    @Test
    fun `separates phrases from keywords`() {

        val phrase = Phrase(
                namedKeyword("keyword"),
                rawValue("value"))

        assert(printTerms(phrase, namedKeyword("keyword")))
                .toBe("(keyword [value]) keyword")
        assert(printTerms(namedKeyword("keyword"), phrase))
                .toBe("keyword (keyword [value])")
    }

    @Test
    fun `separates values`() {
        assert(printTerms(rawValue("first"), rawValue("second")))
                .toBe("[first] [second]")
    }

    @Test
    fun `separates phrases`() {
        assert(
                printTerms(
                        Phrase(namedKeyword("phrase 1")),
                        Phrase(namedKeyword("phrase 2"))))
                .toBe("(phrase 1) (phrase 2)")
    }

}
