package org.predicode.predicator.grammar;

import org.predicode.predicator.terms.Keyword;
import org.predicode.predicator.terms.Variable;

import javax.annotation.Nonnull;


/**
 * Name quoting style.
 */
public enum QuotingStyle {

    /**
     * Automatically quote names when necessary.
     *
     * <p>The opening or closing quote would is added if the name starts with or ends with not allowed symbol
     * respectively.</p>
     *
     * <p>This is used when printing {@link Keyword keyword} name inside a phrase.</p>
     */
    AUTO_QUOTE,

    /**
     * Always add opening quote.
     *
     * <p>The closing quote would is added too if the name ends with not allowed symbol.</p>
     *
     * <p>This is used when printing {@link }org.predicode.predicator.Atom atom}
     * or {@link Variable variable} name inside a phrase.</p>
     */
    OPEN_QUOTE,

    /**
     * Always add both opening abd closing quotes.
     */
    ALWAYS_QUOTE;

    /**
     * Whether to always add opening quote.
     */
    public final boolean openQuote() {
        return this != AUTO_QUOTE;
    }

    /**
     * Whether to always add closing quote.
     */
    public final boolean closeQuote() {
        return this == ALWAYS_QUOTE;
    }

    public boolean printName(
            @Nonnull CharSequence name,
            int quote,
            @Nonnull CodePointPrinter print) {
        return new NamePrinter(name, quote, this, print).print();
    }

    @Nonnull
    public String printName(@Nonnull CharSequence name, int quote) {

        final StringBuilder out = new StringBuilder();

        printName(name, quote, out::appendCodePoint);

        return out.toString();
    }

}
