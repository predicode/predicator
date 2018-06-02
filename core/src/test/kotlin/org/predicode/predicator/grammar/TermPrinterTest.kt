package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.Test
import org.predicode.predicator.*


internal class TermPrinterTest {

    @Test
    fun keyword() {
        assert(print(namedKeyword("keyword name")))
                .toBe("keyword name")
    }

    @Test
    fun atom() {
        assert(print(namedAtom("atom name")))
                .toBe("'atom name")
    }

    @Test
    fun variable() {
        assert(print(namedVariable("variable name")))
                .toBe("_variable name")
    }

    @Test
    fun phrase() {
        assert(print(
                Phrase(
                        namedKeyword("keyword"),
                        namedAtom("atom"))))
                .toBe("(keyword 'atom)")
    }

    @Test
    fun `removes leading whitespaces`() {
        assert(print(namedKeyword(" \t keyword")))
                .toBe("keyword")
        assert(print(namedAtom(" \r atom")))
                .toBe("'atom")
        assert(print(namedVariable(" \n variable")))
                .toBe("_variable")
    }

    @Test
    fun `removes trailing whitespaces`() {
        assert(print(namedKeyword("keyword \t ")))
                .toBe("keyword")
        assert(print(namedAtom("atom \r ")))
                .toBe("'atom")
        assert(print(namedVariable("variable \n ")))
                .toBe("_variable")
    }

    @Test
    fun `removes extra whitespaces`() {
        assert(print(namedKeyword("keyword \r name")))
                .toBe("keyword name")
        assert(print(namedAtom("atom \n name")))
                .toBe("'atom name")
        assert(print(namedVariable("variable \t name ")))
                .toBe("_variable name")
    }

    @Test
    fun `escapes symbols`() {
        assert(print(namedKeyword("keyword:name")))
                .toBe("keyword\\:name")
        assert(print(namedKeyword("keyword\\name")))
                .toBe("keyword\\\\name")
        assert(print(namedAtom("atom'name")))
                .toBe("'atom\\'name")
        assert(print(namedVariable("variable_name")))
                .toBe("_variable\\_name")
    }

    @Test
    fun `closes quotes before keywords`() {
        assert(print(namedAtom("atom"), namedKeyword("keyword")))
                .toBe("'atom' keyword")
        assert(print(
                namedVariable("variable"),
                namedKeyword("keyword")))
                .toBe("_variable_ keyword")
    }

    @Test
    fun `separate keywords`() {
        assert(print(namedKeyword("first"), namedKeyword("second")))
                .toBe("first second")
    }

    @Test
    fun `separates values from quoted terms`() {
        assert(print(rawValue("value"), namedAtom("atom")))
                .toBe("[value] 'atom")
        assert(print(namedAtom("atom"), rawValue("value")))
                .toBe("'atom [value]")
        assert(print(rawValue("value"), namedVariable("variable")))
                .toBe("[value] _variable")
        assert(print(namedVariable("variable"), rawValue("value")))
                .toBe("_variable [value]")
    }

    @Test
    fun `separates phrases from quoted terms`() {

        val phrase = Phrase(
                namedKeyword("keyword"),
                rawValue("value"))

        assert(print(phrase, namedAtom("atom")))
                .toBe("(keyword [value]) 'atom")
        assert(print(namedAtom("atom"), phrase))
                .toBe("'atom (keyword [value])")
        assert(print(phrase, namedVariable("variable")))
                .toBe("(keyword [value]) _variable")
        assert(print(namedVariable("variable"), phrase))
                .toBe("_variable (keyword [value])")
    }

    @Test
    fun `separates values from keywords`() {
        assert(print(rawValue("value"), namedKeyword("keyword")))
                .toBe("[value] keyword")
        assert(print(namedKeyword("keyword"), rawValue("value")))
                .toBe("keyword [value]")
    }

    @Test
    fun `separates phrases from keywords`() {

        val phrase = Phrase(
                namedKeyword("keyword"),
                rawValue("value"))

        assert(print(phrase, namedKeyword("keyword")))
                .toBe("(keyword [value]) keyword")
        assert(print(namedKeyword("keyword"), phrase))
                .toBe("keyword (keyword [value])")
    }

    @Test
    fun `separates values`() {
        assert(print(rawValue("first"), rawValue("second")))
                .toBe("[first] [second]")
    }

    @Test
    fun `separates phrases`() {
        assert(print(
                Phrase(namedKeyword("phrase 1")),
                Phrase(namedKeyword("phrase 2"))))
                .toBe("(phrase 1) (phrase 2)")
    }

    private fun print(vararg terms: Term) = StringBuilder().apply {
        termPrinter { appendCodePoint(it) }.print(*terms)
    }.toString()

}
