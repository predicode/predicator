package org.predicode.predicator.grammar

import org.predicode.predicator.Term
import java.util.function.IntConsumer

const val BACKSLASH = '\\'.toInt()
const val SPACE = ' '.toInt()
const val SINGLE_QUOTE = '\''.toInt()
const val UNDERSCORE = '_'.toInt()
const val OPENING_BRACE = '['.toInt()
const val CLOSING_BRACE = ']'.toInt()
const val OPENING_PARENT = '('.toInt()
const val CLOSING_PARENT = ')'.toInt()

sealed class TermPrinter(protected val print: IntConsumer) {

    fun print(terms: Iterable<Term>): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    fun print(vararg terms: Term): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    open fun keyword(name: CharSequence): TermPrinter = endQuoted().separate().apply {
        name(name)
    }

    fun atom(name: CharSequence): TermPrinter = separate().apply {
        out(SINGLE_QUOTE)
        name(name)
    }.quoted(SINGLE_QUOTE)

    fun variable(name: CharSequence): TermPrinter = separate().apply {
        out(UNDERSCORE)
        name(name)
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

    private fun name(name: CharSequence) {
        NameStart(print).name(name)
    }

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

    private abstract class NamePrinter(protected val print: IntConsumer) {

        fun name(name: CharSequence) {
            name.codePoints().mapToObj { it }.reduce(
                    this,
                    { out, c ->
                        if (Character.isWhitespace(c)) out.space()
                        else when (Character.getType(c)) {
                            in Character.DECIMAL_DIGIT_NUMBER..Character.LETTER_NUMBER, // Letters
                            in Character.UPPERCASE_LETTER..Character.OTHER_LETTER -> // Numbers
                                out.nonSpace().apply {
                                    // Letter, number
                                    out(c)
                                }
                            in Character.DASH_PUNCTUATION..Character.OTHER_PUNCTUATION, // Punctuation
                            in Character.MATH_SYMBOL..Character.OTHER_SYMBOL, // Symbols
                            in Character.NON_SPACING_MARK..Character.COMBINING_SPACING_MARK -> // Marks
                                out.nonSpace().apply {
                                    out(BACKSLASH)
                                    out(c)
                                }
                            else ->
                                out.nonSpace().apply {
                                    out(BACKSLASH)
                                    Integer.toHexString(c).forEach { out(it.toInt()) }
                                    out(BACKSLASH)
                                }
                        }
                    }) { _, s -> s }
        }

        protected abstract fun space(): NamePrinter

        protected abstract fun nonSpace(): NamePrinter

        protected fun out(codePoint: Int) = print.accept(codePoint)

    }

    private class NameStart(print: IntConsumer) : NamePrinter(print) {

        override fun space() = this

        override fun nonSpace() = NameBody(print)

    }

    private class NameBody(print: IntConsumer) : NamePrinter(print) {

        override fun space(): NamePrinter =
                NameSpace(print)

        override fun nonSpace(): NamePrinter = this

    }

    private class NameSpace(print: IntConsumer) : NamePrinter(print) {

        override fun space(): NamePrinter = this

        override fun nonSpace() = NameBody(print).apply { out(SPACE) }

    }

    companion object {

        @JvmStatic
        fun termPrinter(print: IntConsumer): TermPrinter =
                InitialTermPrinter(print)

    }

}

fun termPrinter(print: (Int) -> Unit): TermPrinter =
        TermPrinter.termPrinter(IntConsumer { print(it) })
