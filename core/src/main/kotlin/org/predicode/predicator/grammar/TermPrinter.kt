package org.predicode.predicator.grammar

import org.predicode.predicator.Term
import java.util.function.IntConsumer

class TermPrinter private constructor(private val print: IntConsumer) {

    private var partPrinter: PartPrinter = InitialTermPrinter

    fun print(terms: Iterable<Term>) {
        terms.forEach { term -> term.print(this) }
    }

    fun print(vararg terms: Term) {
        terms.forEach { term -> term.print(this) }
    }

    fun keyword(name: CharSequence) {
        partPrinter = partPrinter.keyword(this, name)
    }

    fun atom(name: CharSequence) {
        this.partPrinter = this.partPrinter.separate(this).apply {
            printName(name, print, SINGLE_QUOTE, quoting = QuotingStyle.OPEN_QUOTE)
        }.quoted(SINGLE_QUOTE)
    }

    fun variable(name: CharSequence) {
        this.partPrinter = this.partPrinter.separate(this).apply {
            printName(name, print, UNDERSCORE, quoting = QuotingStyle.OPEN_QUOTE)
        }.quoted(UNDERSCORE)
    }

    fun value(value: CharSequence) {
        this.partPrinter = this.partPrinter.separate(this).apply {
            out(OPENING_BRACE)
            value.codePoints().forEach { out(it) }
            out(CLOSING_BRACE)
        }
    }

    fun startCompound() {
        this.partPrinter = this.partPrinter.endQuoted(this).separate(this).let {
            out(OPENING_PARENT)
            InitialTermPrinter
        }
    }

    fun endCompound() {
        this.partPrinter = this.partPrinter.endQuoted(this).apply {
            out(CLOSING_PARENT)
        }
    }

    private fun out(codePoint: CodePoint) = print.accept(codePoint)

    private interface PartPrinter {

        fun separate(printer: TermPrinter): PartPrinter

        fun endQuoted(printer: TermPrinter): PartPrinter

        @JvmDefault
        fun keyword(printer: TermPrinter, name: CharSequence): PartPrinter =
                endQuoted(printer).separate(printer).apply {
                    printName(name, printer.print, BACKTICK)
                }

        @JvmDefault
        fun quoted(quote: CodePoint): PartPrinter =
                QuotedTermPrinter(quote)

    }

    private object InitialTermPrinter : PartPrinter {

        override fun separate(printer: TermPrinter) = UnquotedTermPrinter

        override fun endQuoted(printer: TermPrinter) = this

    }

    private class QuotedTermPrinter(val quote: Int) : PartPrinter {

        override fun separate(printer: TermPrinter) = endQuoted(printer).apply { printer.out(SPACE) }

        override fun keyword(printer: TermPrinter, name: CharSequence): PartPrinter {
            printer.out(quote)
            return super.keyword(printer, name).endQuoted(printer)
        }

        override fun endQuoted(printer: TermPrinter) = UnquotedTermPrinter

    }

    private object UnquotedTermPrinter : PartPrinter {

        override fun separate(printer: TermPrinter) = apply { printer.out(SPACE) }

        override fun endQuoted(printer: TermPrinter) = this

    }

    companion object {

        @JvmStatic
        fun printTerms(print: IntConsumer, terms: Iterable<Term>) {
            TermPrinter(print).print(terms)
        }

        @JvmStatic
        fun printTerms(terms: Iterable<Term>): String = StringBuilder().apply {
            printTerms(print = IntConsumer { appendCodePoint(it) }, terms = terms)
        }.toString()

        @JvmStatic
        fun printTerms(print: IntConsumer, vararg terms: Term) {
            TermPrinter(print).print(*terms)
        }

        @JvmStatic
        fun printTerms(vararg terms: Term): String = StringBuilder().apply {
            printTerms(print = IntConsumer { appendCodePoint(it) }, terms = *terms)
        }.toString()

    }

}

fun printTerms(terms: Iterable<Term>, print: (CodePoint) -> Unit) {
    TermPrinter.printTerms(IntConsumer(print), terms)
}

fun printTerms(terms: Iterable<Term>): String = TermPrinter.printTerms(terms)

fun printTerms(vararg terms: Term, print: (CodePoint) -> Unit) {
    TermPrinter.printTerms(IntConsumer(print), *terms)
}

fun printTerms(vararg terms: Term): String = TermPrinter.printTerms(*terms)
