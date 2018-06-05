package org.predicode.predicator.grammar

internal class NamePrinter(
        val name: CharSequence,
        val quote: CodePoint,
        val quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE,
        private val print: CodePointConsumer) {

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

    fun out(codePoint: Int) = print(codePoint)

}

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE,
        print: CodePointConsumer) = NamePrinter(
        name,
        print = print,
        quote = quote,
        quoting = quoting).print()

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE): String = StringBuilder().apply {
    printName(name, quote = quote, quoting = quoting) { appendCodePoint(it) }
}.toString()
