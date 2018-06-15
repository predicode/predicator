package org.predicode.predicator.grammar

import org.predicode.predicator.Term

typealias CodePoint = Int

fun printTerms(terms: Iterable<Term>, print: (CodePoint) -> Unit) =
        TermPrinter.printTerms(terms, print)

fun printTerms(terms: Iterable<Term>): String =
        TermPrinter.printTerms(terms)

fun printTerms(vararg terms: Term, print: (CodePoint) -> Unit) =
        TermPrinter.printTerms(print, terms)

fun printTerms(vararg terms: Term): String =
        TermPrinter.printTerms(*terms)

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE,
        print: (CodePoint) -> Unit) =
        quoting.printName(name, quote, print)

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE) =
        quoting.printName(name, quote)
