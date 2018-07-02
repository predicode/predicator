package org.predicode.predicator.grammar

import org.predicode.predicator.terms.Term

typealias CodePoint = Int

fun printTerms(terms: Iterable<Term>): String =
        TermPrinter.printTerms(terms)

fun printTerms(terms: Iterable<Term>, print: (CodePoint) -> Unit) =
        TermPrinter.printTerms(terms, print)

fun StringBuilder.appendTerms(terms: Iterable<Term>) = apply {
    TermPrinter.printTerms(terms, this)
}

fun printTerms(vararg terms: Term, print: (CodePoint) -> Unit) =
        TermPrinter.printTerms(print, terms)

fun printTerms(vararg terms: Term): String =
        TermPrinter.printTerms(*terms)

fun StringBuilder.appendTerms(vararg terms: Term) = apply {
    TermPrinter.printTerms(this, *terms)
}

fun printName(
        name: CharSequence,
        quoted: QuotedName,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE) =
        quoting.printName(name, quoted)

fun printName(
        name: CharSequence,
        quoted: QuotedName,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE,
        print: (CodePoint) -> Unit) =
        quoting.printName(name, quoted, print)

fun StringBuilder.appendName(
        name: CharSequence,
        quoted: QuotedName,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE) = apply {
    quoting.printName(name, quoted, this)
}
