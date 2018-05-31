package org.predicode.predicator

import java.util.function.Consumer

sealed class TermPrinter(protected val print: Consumer<CharSequence>) {

    fun print(terms: Iterable<Term>): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    fun print(vararg terms: Term): TermPrinter =
            terms.fold(this) { out, term -> term.print(out) }

    open fun keyword(name: CharSequence): TermPrinter = endQuoted().separate().apply {
        name(name)
    }

    fun atom(name: CharSequence): TermPrinter = separate().apply {
        out("'")
        name(name)
    }.quoted("'")

    fun variable(name: CharSequence): TermPrinter = separate().apply {
        out("_")
        name(name)
    }.quoted("_")

    fun value(value: CharSequence): TermPrinter = separate().apply {
        out("[")
        out(value)
        out("]")
    }

    fun startCompound(): TermPrinter = endQuoted().separate().let {
        out("(")
        InitialTermPrinter(print)
    }

    fun endCompound(): TermPrinter = endQuoted().apply { out(")") }

    private fun name(name: CharSequence) {
        NameStart(print).name(name)
    }

    private fun quoted(quote: CharSequence): TermPrinter = QuotedTermPrinter(print, quote)

    protected abstract fun separate(): TermPrinter

    protected abstract fun endQuoted(): TermPrinter

    protected fun out(text: CharSequence) = print.accept(text)

    private class InitialTermPrinter(print: Consumer<CharSequence>) : TermPrinter(print) {

        override fun separate() = UnquotedTermPrinter(print)

        override fun endQuoted() = this

    }

    private class QuotedTermPrinter(
            print: Consumer<CharSequence>,
            val quote: CharSequence) : TermPrinter(print) {

        override fun separate() = endQuoted().apply { out(" ") }

        override fun keyword(name: CharSequence): TermPrinter {
            out(quote)
            return super.keyword(name).endQuoted()
        }

        override fun endQuoted() = UnquotedTermPrinter(print)

    }

    private class UnquotedTermPrinter(print: Consumer<CharSequence>) : TermPrinter(print) {

        override fun separate() = apply { out(" ") }

        override fun endQuoted() = this

    }

    private abstract class NamePrinter(protected val print: Consumer<CharSequence>) {

        fun name(name: CharSequence) {
            name.fold(this) { out, c ->
                if (c.isWhitespace()) out.space()
                else when (c.category.code[0]) {
                    'L', 'N' -> out.nonSpace().apply {
                        // Letter, number
                        out(c.toString())
                    }
                    'P', 'S', 'M' -> out.nonSpace().apply {
                        // Punctuation, symbol, mark
                        out("\\$c")
                    }
                    else -> out.nonSpace().apply {
                        out(when {
                            c.toInt() < 32 -> "\\${c.toInt()}"
                            else -> "\\u${c.toInt().toString(16).padStart(4, '0')}"
                        })
                    }
                }
            }
        }

        protected abstract fun space(): NamePrinter

        protected abstract fun nonSpace(): NamePrinter

        protected fun out(text: CharSequence) = print.accept(text)

    }

    private class NameStart(print: Consumer<CharSequence>) : NamePrinter(print) {

        override fun space() = this

        override fun nonSpace() = NameBody(print)

    }

    private class NameBody(print: Consumer<CharSequence>) : NamePrinter(print) {

        override fun space(): NamePrinter = NameSpace(print)

        override fun nonSpace(): NamePrinter = this

    }

    private class NameSpace(print: Consumer<CharSequence>) : NamePrinter(print) {

        override fun space(): NamePrinter = this

        override fun nonSpace() = NameBody(print).apply { out(" ") }

    }

    companion object {

        @JvmStatic
        fun termPrinter(print: Consumer<CharSequence>): TermPrinter = InitialTermPrinter(print)

    }

}

fun termPrinter(print: (CharSequence) -> Unit): TermPrinter = TermPrinter.termPrinter(Consumer { print(it) })
