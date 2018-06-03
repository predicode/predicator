@file:JvmName("TermNames")
package org.predicode.predicator.grammar

import java.util.function.IntConsumer

internal class NamePrinter(
        val name: CharSequence,
        private val print: IntConsumer,
        val quote: CodePoint,
        val quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE) {

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
        if (quoting.closeQuote || !lastNonSeparating.nameEnd) out(quote)
    }

    fun out(codePoint: Int) = print.accept(codePoint)

}

fun printName(
        name: CharSequence,
        print: IntConsumer,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE) = NamePrinter(
        name,
        print = print,
        quote = quote,
        quoting = quoting).print()

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE): String = StringBuilder().apply {
    printName(name, print = IntConsumer { appendCodePoint(it) }, quote = quote, quoting = quoting)
}.toString()
