package org.predicode.predicator.grammar

import java.util.*


private val OPERATORS: BitSet = BitSet(128).apply {
    set('%'.toInt())
    set('&'.toInt())
    set('|'.toInt())
    set('+'.toInt())
    set('-'.toInt())
    set('*'.toInt())
    set('/'.toInt())
    set('>'.toInt())
    set('<'.toInt())
    set('='.toInt())
    set('~'.toInt())
    set('^'.toInt())
}

private val BUILTINS: BitSet = BitSet(128).apply {
    set(':'.toInt())
    set('#'.toInt())
    set('$'.toInt())
    set('@'.toInt())
}

private val QUOTES: BitSet = BitSet(128).apply {
    set(BACKTICK)
    set(SINGLE_QUOTE)
    set(DOUBLE_QUOTE)
    set(UNDERSCORE)
}

enum class CharClass(
        private val printer: NameCharPrinter,
        val nameStart: Boolean = false,
        val nameEnd: Boolean = false) {

    START_CHAR_CLASS(NameStartPrinter, nameStart = true, nameEnd = true) {
        override fun nameGluesWith(next: CharClass) = true
    },
    LETTER_CHAR_CLASS(NameBodyPrinter, nameStart = true, nameEnd = true),
    NUMERIC_CHAR_CLASS(NameBodyPrinter, nameEnd = true),
    OPERATOR_CHAR_CLASS(NameBodyPrinter),
    CONNECTOR_CHAR_CLASS(NameBodyPrinter),
    SEPARATOR_CHAR_CLASS(NameSeparatorPrinter, nameStart = true, nameEnd = true) {
        override fun nameGluesWith(next: CharClass) = true
    },
    BUILTIN_CHAR_CLASS(NameEscapePrinter),
    QUOTE_CHAR_CLASS(NameEscapePrinter),
    ESCAPE_CHAR_CLASS(NameEscapePrinter),
    PUNCTUATION_CHAR_CLASS(NameEscapePrinter),
    OTHER_CHAR_CLASS(NameCodePrinter);

    val separating: Boolean
        get() = this == SEPARATOR_CHAR_CLASS

    open fun nameGluesWith(next: CharClass) = next != this

    internal fun printName(out: NamePrinter, codePoint: CodePoint): CharClass =
            of(codePoint).let { next ->
                printer.append(this, out, next).also { appender ->
                    appender.printer.print(appender, out, next, codePoint)
                }
            }

    private interface NameCharPrinter {

        fun append(current: CharClass, out: NamePrinter, next: CharClass): CharClass

        fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint)

    }

    private object NameStartPrinter : NameCharPrinter {

        override fun append(current: CharClass, out: NamePrinter, next: CharClass) =
                if (next.separating) current // Skip leading separators
                else {
                    // Open quote if the name does not start with allowed start symbol
                    if (out.quoting.openQuote || !next.nameStart) out.out(out.quote)
                    next
                }

        override fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint) {
            // Never prints anything
        }

    }

    private object NameSeparatorPrinter : NameCharPrinter {

        override fun append(current: CharClass, out: NamePrinter, next: CharClass) =
            if (next.separating) current // Skip subsequent separators
            else {
                // Separate next with space, unless it glues with previous one
                if (!out.lastNonSeparating.nameGluesWith(next)) out.out(SPACE)
                next
            }

        override fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint) {
            // Never prints anything
        }

    }

    private object NameBodyPrinter : NameCharPrinter {

        override fun append(current: CharClass, out: NamePrinter, next: CharClass) = next

        override fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint) {
            out.out(codePoint)
        }

    }

    private object NameEscapePrinter : NameCharPrinter {

        override fun append(current: CharClass, out: NamePrinter, next: CharClass) = next

        override fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint) {
            out.out(BACKSLASH)
            out.out(codePoint)
        }

    }

    private object NameCodePrinter : NameCharPrinter {

        override fun append(current: CharClass, out: NamePrinter, next: CharClass) = next

        override fun print(current: CharClass, out: NamePrinter, next: CharClass, codePoint: CodePoint) {
            out.out(BACKSLASH)
            Integer.toHexString(codePoint).forEach { out.out(it.toInt()) }
            out.out(BACKSLASH)
        }

    }

    companion object {

        @JvmStatic
        fun of(codePoint: CodePoint): CharClass = when {
            Character.isWhitespace(codePoint) -> SEPARATOR_CHAR_CLASS
            OPERATORS.get(codePoint) -> OPERATOR_CHAR_CLASS
            QUOTES.get(codePoint) -> QUOTE_CHAR_CLASS
            BUILTINS.get(codePoint) -> BUILTIN_CHAR_CLASS
            codePoint == BACKSLASH -> ESCAPE_CHAR_CLASS
            else -> when (Character.getType(codePoint)) {
                in Character.UPPERCASE_LETTER..Character.OTHER_LETTER -> LETTER_CHAR_CLASS
                in Character.DECIMAL_DIGIT_NUMBER..Character.LETTER_NUMBER -> NUMERIC_CHAR_CLASS
                in Character.MATH_SYMBOL..Character.OTHER_SYMBOL -> OPERATOR_CHAR_CLASS
                Character.DASH_PUNCTUATION.toInt(), Character.CONNECTOR_PUNCTUATION.toInt() -> CONNECTOR_CHAR_CLASS
                in Character.START_PUNCTUATION..Character.OTHER_PUNCTUATION -> PUNCTUATION_CHAR_CLASS
                in Character.SPACE_SEPARATOR..Character.PARAGRAPH_SEPARATOR -> SEPARATOR_CHAR_CLASS
                else -> OTHER_CHAR_CLASS
            }
        }

    }

}
