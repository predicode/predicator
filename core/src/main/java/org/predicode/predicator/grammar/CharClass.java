package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.BACKSLASH;
import static org.predicode.predicator.grammar.NameCharPrinter.*;
import static org.predicode.predicator.grammar.NameCharPrinter.NAME_ESCAPE_PRINTER;


public enum CharClass {

    START_CHAR_CLASS(NAME_START_PRINTER, true, true) {
        @Override
        boolean nameGluesWith(@Nonnull CharClass next) {
            return true;
        }
    },
    LETTER_CHAR_CLASS(NAME_BODY_PRINTER, true, true) {
        @Override
        boolean nameGluesWith(@Nonnull CharClass next) {
            return super.nameGluesWith(next) && next != NUMERIC_CHAR_CLASS;
        }
    },
    NUMERIC_CHAR_CLASS(NAME_BODY_PRINTER, false, true) {
        @Override
        boolean nameGluesWith(@Nonnull CharClass next) {
            return super.nameGluesWith(next) && next != LETTER_CHAR_CLASS;
        }
    },
    OPERATOR_CHAR_CLASS(NAME_BODY_PRINTER, false, false),
    CONNECTOR_CHAR_CLASS(NAME_BODY_PRINTER, false, false),
    SEPARATOR_CHAR_CLASS(NAME_SEPARATOR_PRINTER, true, true) {
        @Override
        boolean nameGluesWith(@Nonnull CharClass next) {
            return true;
        }
    },
    BUILTIN_CHAR_CLASS(NAME_ESCAPE_PRINTER, false, false),
    QUOTE_CHAR_CLASS(NAME_ESCAPE_PRINTER, false, false),
    ESCAPE_CHAR_CLASS(NAME_ESCAPE_PRINTER, false, false),
    PUNCTUATION_CHAR_CLASS(NAME_ESCAPE_PRINTER, false, false),
    OTHER_CHAR_CLASS(NAME_CODE_PRINTER, false, false);

    @Nonnull
    public static CharClass of(int codePoint) {
        if (Character.isWhitespace(codePoint)) {
            return SEPARATOR_CHAR_CLASS;
        }
        if (OPERATORS.get(codePoint)) {
            return OPERATOR_CHAR_CLASS;
        }
        if (QUOTES.get(codePoint)) {
            return QUOTE_CHAR_CLASS;
        }
        if (BUILTINS.get(codePoint)) {
            return BUILTIN_CHAR_CLASS;
        }
        if (codePoint == BACKSLASH) {
            return ESCAPE_CHAR_CLASS;
        }

        final int type = Character.getType(codePoint);

        if (type >= Character.UPPERCASE_LETTER && type <= Character.OTHER_LETTER) {
            return LETTER_CHAR_CLASS;
        }
        if (type >= Character.DECIMAL_DIGIT_NUMBER && type <= Character.LETTER_NUMBER) {
            return NUMERIC_CHAR_CLASS;
        }
        if (type >= Character.MATH_SYMBOL && type <= Character.OTHER_SYMBOL) {
            return OPERATOR_CHAR_CLASS;
        }
        if (type == Character.DASH_PUNCTUATION || type == Character.CONNECTOR_PUNCTUATION) {
            return CONNECTOR_CHAR_CLASS;
        }
        if (type >= Character.START_PUNCTUATION && type <= Character.OTHER_PUNCTUATION) {
            return PUNCTUATION_CHAR_CLASS;
        }
        if (type >= Character.SPACE_SEPARATOR && type <= Character.PARAGRAPH_SEPARATOR) {
            return SEPARATOR_CHAR_CLASS;
        }

        return OTHER_CHAR_CLASS;
    }

    @Nonnull
    final NameCharPrinter printer;

    private final boolean nameStart;
    private final boolean nameEnd;

    CharClass(
            @Nonnull NameCharPrinter printer,
            boolean nameStart,
            boolean nameEnd) {
        this.printer = printer;
        this.nameStart = nameStart;
        this.nameEnd = nameEnd;
    }

    public final boolean nameStart() {
        return this.nameStart;
    }

    public final boolean nameEnd() {
        return this.nameEnd;
    }

    public final boolean separating() {
        return this == SEPARATOR_CHAR_CLASS;
    }

    boolean nameGluesWith(@Nonnull CharClass next) {
        return next != this;
    }

    @Nonnull
    CharClass printName(@Nonnull NamePrinter out, int codePoint) {

        final CharClass next = of(codePoint);

        final CharClass appender = this.printer.append(this, out, next);

        appender.printer.print(appender, out, next, codePoint);

        return appender;
    }

}


