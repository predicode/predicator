package org.predicode.predicator.grammar

import org.predicode.predicator.Term
import java.util.function.IntConsumer

sealed class TermPrinter(protected val print: IntConsumer) {

    fun print(terms: Iterable<Term>): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    fun print(vararg terms: Term): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    open fun keyword(name: CharSequence): TermPrinter = endQuoted().separate().apply {
        printName(name, print, '`')
    }

    fun atom(name: CharSequence): TermPrinter = separate().apply {
        printName(name, print, '\'', openQuote = true)
    }.quoted(SINGLE_QUOTE)

    fun variable(name: CharSequence): TermPrinter = separate().apply {
        printName(name, print, '_', openQuote = true)
    }.quoted(UNDERSCORE)

    fun value(value: CharSequence): TermPrinter = separate().apply {
        out(OPENING_BRACE)
        value.codePoints().forEach { out(it) }
        out(CLOSING_BRACE)
    }

    fun startCompound(): TermPrinter = endQuoted().separate().let {
        out(OPENING_PARENT)
        InitialTermPrinter(print)
    }

    fun endCompound(): TermPrinter = endQuoted().apply { out(CLOSING_PARENT) }

    private fun quoted(quote: Int): TermPrinter =
            QuotedTermPrinter(print, quote)

    protected abstract fun separate(): TermPrinter

    protected abstract fun endQuoted(): TermPrinter

    protected fun out(codePoint: Int) = print.accept(codePoint)

    private class InitialTermPrinter(print: IntConsumer) : TermPrinter(print) {

        override fun separate() = UnquotedTermPrinter(print)

        override fun endQuoted() = this

    }

    private class QuotedTermPrinter(
            print: IntConsumer,
            val quote: Int) : TermPrinter(print) {

        override fun separate() = endQuoted().apply { out(SPACE) }

        override fun keyword(name: CharSequence): TermPrinter {
            out(quote)
            return super.keyword(name).endQuoted()
        }

        override fun endQuoted() = UnquotedTermPrinter(print)

    }

    private class UnquotedTermPrinter(print: IntConsumer) : TermPrinter(print) {

        override fun separate() = apply { out(SPACE) }

        override fun endQuoted() = this

    }

    companion object {

        @JvmStatic
        fun termPrinter(print: IntConsumer): TermPrinter =
                InitialTermPrinter(print)

    }

}

fun termPrinter(print: (Int) -> Unit): TermPrinter =
        TermPrinter.termPrinter(IntConsumer { print(it) })
