package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.BACKTICK;
import static org.predicode.predicator.grammar.CodePoints.SPACE;


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

    @Nonnull
    PartPrinter separate(@Nonnull TermPrinter printer);

    @Nonnull
    PartPrinter endQuoted(@Nonnull TermPrinter printer);

    @Nonnull
    default PartPrinter keyword(@Nonnull TermPrinter printer, @Nonnull CharSequence name) {

        final PartPrinter out = endQuoted(printer).separate(printer);

        QuotingStyle.AUTO_QUOTE.printName(name, BACKTICK, printer::print);

        return out;
    }

    @Nonnull
    default PartPrinter quoted(int quote) {
        return new QuotedTermPrinter(quote);
    }

    final class QuotedTermPrinter implements PartPrinter {

        private final int quote;

        QuotedTermPrinter(int quote) {
            this.quote = quote;
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
