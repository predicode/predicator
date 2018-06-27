package org.predicode.predicator.grammar;

import static org.predicode.predicator.grammar.CodePoints.*;


/**
 * A kind of quoted name used for various terms.
 */
public enum QuotedName {

    ATOM_NAME(SINGLE_QUOTE),
    VARIABLE_NAME(UNDERSCORE),
    KEYWORD_NAME(BACKTICK),
    PREFIX_OPERATOR_NAME(BACKTICK),
    INFIX_OPERATOR_NAME(BACKTICK);

    private final int quote;

    QuotedName(int quote) {
        this.quote = quote;
    }

    public final int getQuote() {
        return this.quote;
    }

    public final boolean isKeyword() {
        return ordinal() >= KEYWORD_NAME.ordinal();
    }

    public final boolean isPrefix() {
        return this == PREFIX_OPERATOR_NAME;
    }

    public final boolean isInfix() {
        return this == INFIX_OPERATOR_NAME;
    }

}
