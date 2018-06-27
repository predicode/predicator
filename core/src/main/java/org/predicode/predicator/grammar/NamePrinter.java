package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;


class NamePrinter implements CodePointPrinter {

    @Nonnull
    private final CharSequence name;

    @Nonnull
    private final QuotedName quoted;

    @Nonnull
    private final QuotingStyle quoting;

    @Nonnull
    private final CodePointPrinter print;

    @Nonnull
    private CharClass lastNonSeparating = CharClass.START_CHAR_CLASS;

    NamePrinter(
            @Nonnull CharSequence name,
            @Nonnull QuotedName quoted,
            @Nonnull QuotingStyle quoting,
            @Nonnull CodePointPrinter print) {
        this.name = name;
        this.quoted = quoted;
        this.quoting = quoting;
        this.print = print;
    }

    @Nonnull
    final QuotedName getQuoted() {
        return this.quoted;
    }

    @Nonnull
    final QuotingStyle getQuoting() {
        return this.quoting;
    }

    @Nonnull
    final CharClass getLastNonSeparating() {
        return this.lastNonSeparating;
    }

    @Override
    public void print(int codePoint) {
        this.print.print(codePoint);
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

        if (this.quoting.closeQuote() || !this.lastNonSeparating.nameEnd(this.quoted)) {
            // Close quote if the name does not end with allowed symbol
            print(this.quoted.getQuote());
            return true;
        }

        return false;
    }

}
