package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.*;
import static org.predicode.predicator.grammar.QuotedName.ATOM_NAME;
import static org.predicode.predicator.grammar.QuotedName.UNQUOTED_NAME;
import static org.predicode.predicator.grammar.QuotedName.VARIABLE_NAME;


interface PartPrinter {

    PartPrinter INITIAL_PART_PRINTER = new PartPrinter() {

        @Nonnull
        @Override
        public QuotedName getQuoted() {
            return UNQUOTED_NAME;
        }

        @Nonnull
        @Override
        public PartPrinter separate(@Nonnull TermPrinter printer) {
            return UNQUOTED_PART_PRINTER;
        }

        @Nonnull
        @Override
        public PartPrinter endQuoted(@Nonnull TermPrinter printer) {
            return this;
        }

    };

    PartPrinter UNQUOTED_PART_PRINTER = new UnquotedPartPrinter(UNQUOTED_NAME);
    QuotedPartPrinter ATOM_PART_PRINTER = new QuotedPartPrinter(ATOM_NAME);
    QuotedPartPrinter VARIABLE_PART_PRINTER = new QuotedPartPrinter(VARIABLE_NAME);

    @Nonnull
    QuotedName getQuoted();

    @Nonnull
    PartPrinter separate(@Nonnull TermPrinter printer);

    @Nonnull
    PartPrinter endQuoted(@Nonnull TermPrinter printer);

    @Nonnull
    default PartPrinter keyword(
            @Nonnull TermPrinter printer,
            @Nonnull CharSequence name,
            @Nonnull QuotedName quoted) {

        PartPrinter out = endQuoted(printer);

        if (!getQuoted().isPrefix() && !quoted.isInfix()) {
            out = out.separate(printer);
        }

        QuotingStyle.AUTO_QUOTE.printName(name, BACKTICK, printer);

        return out;
    }

    final class QuotedPartPrinter implements PartPrinter {

        @Nonnull
        private final UnquotedPartPrinter unquoted;

        private QuotedPartPrinter(@Nonnull QuotedName quoted) {
            this.unquoted = new UnquotedPartPrinter(quoted);
        }

        @Override
        @Nonnull
        public QuotedName getQuoted() {
            return this.unquoted.getQuoted();
        }

        final int getQuote() {
            return getQuoted().getQuote();
        }

        @Nonnull
        @Override
        public PartPrinter separate(@Nonnull TermPrinter printer) {

            final PartPrinter result = endQuoted(printer);

            printer.print(SPACE);

            return result;
        }

        @Nonnull
        @Override
        public PartPrinter endQuoted(@Nonnull TermPrinter printer) {
            return this.unquoted;
        }

        @Nonnull
        @Override
        public PartPrinter keyword(
                @Nonnull TermPrinter printer,
                @Nonnull CharSequence name,
                @Nonnull QuotedName quoted) {
            printer.print(getQuote());
            return PartPrinter.super.keyword(printer, name, quoted).endQuoted(printer);
        }

    }

    final class UnquotedPartPrinter implements PartPrinter {

        @Nonnull
        private final QuotedName quoted;

        UnquotedPartPrinter(@Nonnull QuotedName quoted) {
            this.quoted = quoted;
        }

        @Override
        @Nonnull
        public QuotedName getQuoted() {
            return this.quoted;
        }

        @Nonnull
        @Override
        public PartPrinter separate(@Nonnull TermPrinter printer) {
            printer.print(SPACE);
            return this;
        }

        @Nonnull
        @Override
        public PartPrinter endQuoted(@Nonnull TermPrinter printer) {
            return this;
        }

    }


}
