package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.*;


interface PartPrinter {

    PartPrinter INITIAL_PART_PRINTER = new PartPrinter() {

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

    PartPrinter UNQUOTED_PART_PRINTER = new PartPrinter() {

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

    };

    QuotedPartPrinter ATOM_PART_PRINTER = new QuotedPartPrinter(SINGLE_QUOTE);
    QuotedPartPrinter VARIABLE_PART_PRINTER = new QuotedPartPrinter(UNDERSCORE);

    @Nonnull
    PartPrinter separate(@Nonnull TermPrinter printer);

    @Nonnull
    PartPrinter endQuoted(@Nonnull TermPrinter printer);

    @Nonnull
    default PartPrinter keyword(@Nonnull TermPrinter printer, @Nonnull CharSequence name) {

        final PartPrinter out = endQuoted(printer).separate(printer);

        QuotingStyle.AUTO_QUOTE.printName(name, BACKTICK, printer);

        return out;
    }

    final class QuotedPartPrinter implements PartPrinter {

        private final int quote;

        private QuotedPartPrinter(int quote) {
            this.quote = quote;
        }

        final int getQuote() {
            return this.quote;
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
            return UNQUOTED_PART_PRINTER;
        }

        @Nonnull
        @Override
        public PartPrinter keyword(@Nonnull TermPrinter printer, @Nonnull CharSequence name) {
            printer.print(this.quote);
            return PartPrinter.super.keyword(printer, name).endQuoted(printer);
        }

    }

}
