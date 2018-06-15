package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;


class NamePrinter {

    @Nonnull
    private final CharSequence name;

    private final int quote;

    @Nonnull
    private final QuotingStyle quoting;

    @Nonnull
    private final CodePointPrinter print;

    @Nonnull
    private CharClass lastNonSeparating = CharClass.START_CHAR_CLASS;

    NamePrinter(
            @Nonnull CharSequence name,
            int quote,
            @Nonnull QuotingStyle quoting,
            @Nonnull CodePointPrinter print) {
        this.name = name;
        this.quote = quote;
        this.quoting = quoting;
        this.print = print;
    }

    int getQuote() {
        return this.quote;
    }

    @Nonnull
    final QuotingStyle getQuoting() {
        return this.quoting;
    }

    @Nonnull
    final CharClass getLastNonSeparating() {
        return this.lastNonSeparating;
    }

    boolean print() {
        //noinspection ResultOfMethodCallIgnored
        this.name.codePoints().boxed().reduce(
                CharClass.START_CHAR_CLASS,
                (cclass, c) -> {

                    final CharClass pclass = cclass.printName(this, c);

                    if (!pclass.separating()) {
                        this.lastNonSeparating = pclass;
                    }

                    return pclass;
                },
                (f, s) -> s);

        if (this.quoting.closeQuote() || !this.lastNonSeparating.nameEnd()) {
            // Close quote if the name does not end with allowed symbol
            out(this.quote);
            return true;
        }

        return false;
    }

    void out(int codePoint) {
        this.print.print(codePoint);
    }

}
