package org.predicode.predicator.grammar

import java.util.function.IntConsumer

internal class NamePrinter(
        val name: CharSequence,
        private val print: IntConsumer,
        val quote: CodePoint,
        val openQuote: Boolean,
        val closeQuote: Boolean) {

    var lastNonSeparating: CharClass = CharClass.START_CHAR_CLASS

    fun print() {
        name.codePoints().mapToObj { it }.reduce(
                CharClass.START_CHAR_CLASS,
                { cclass, c ->
                    cclass.printName(this, c).also {
                        if (!it.separating) lastNonSeparating = it
                    }
                },
                { _, s -> s })
        // Close quote if the name does not end with allowed symbol
        if (closeQuote || !lastNonSeparating.nameEnd) out(quote)
    }

    fun out(codePoint: Int) = print.accept(codePoint)

}

fun printName(
        name: CharSequence,
        print: IntConsumer,
        quote: Char,
        openQuote: Boolean = false,
        closeQuote: Boolean = false) = NamePrinter(
        name,
        print = print,
        quote = quote.toInt(),
        openQuote = openQuote, closeQuote = closeQuote).print()

fun printName(
        name: CharSequence,
        quote: Char,
        openQuote: Boolean = false,
        closeQuote: Boolean = false,
        print: (CodePoint) -> Unit) = printName(
        name,
        print = IntConsumer(print),
        quote = quote,
        openQuote = openQuote,
        closeQuote = closeQuote)
