package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.SPACE;
import static org.predicode.predicator.grammar.QuotedName.ATOM_NAME;
import static org.predicode.predicator.grammar.QuotedName.KEYWORD_NAME;
import static org.predicode.predicator.grammar.QuotedName.VARIABLE_NAME;
import static org.predicode.predicator.grammar.QuotingStyle.AUTO_QUOTE;
import static org.predicode.predicator.grammar.QuotingStyle.OPEN_QUOTE;


interface TermSep {

    TermSep NO_SEP = new TermSep() {

        @Override
        public void value(@Nonnull CodePointPrinter out) {
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            return AUTO_QUOTE;
        }

    };

    TermSep SPACE_SEP = new TermSep() {

        @Override
        public void value(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            return AUTO_QUOTE;
        }

    };

    QuotedSep VARIABLE_SEP = new QuotedSep(VARIABLE_NAME.getQuote());
    QuotedSep ATOM_SEP = new QuotedSep(ATOM_NAME.getQuote());

    TermSep KEYWORD_SEP = new TermSep() {

        @Override
        public void value(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
            out.print(KEYWORD_NAME.getQuote());
            out.print(SPACE);
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            return OPEN_QUOTE;
        }

    };

    TermSep PREFIX_SEP = new TermSep() {

        @Override
        public void value(@Nonnull CodePointPrinter out) {
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
            out.print(KEYWORD_NAME.getQuote());
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
            out.print(KEYWORD_NAME.getQuote());
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            out.print(KEYWORD_NAME.getQuote());
            return AUTO_QUOTE;
        }

    };

    TermSep SPECIAL_SEP = new TermSep() {

        @Override
        public void value(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            return OPEN_QUOTE;
        }

    };

    void value(@Nonnull CodePointPrinter out);

    void special(@Nonnull CodePointPrinter out);

    void quoted(@Nonnull CodePointPrinter out);

    void keyword(@Nonnull CodePointPrinter out);

    @Nonnull
    QuotingStyle infix(@Nonnull CodePointPrinter out);

    final class QuotedSep implements TermSep {

        private final int quote;

        QuotedSep(int quote) {
            this.quote = quote;
        }

        public int getQuote() {
            return this.quote;
        }

        @Override
        public void value(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void special(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void quoted(@Nonnull CodePointPrinter out) {
            out.print(SPACE);
        }

        @Override
        public void keyword(@Nonnull CodePointPrinter out) {
            out.print(this.quote);
            out.print(SPACE);
        }

        @Nonnull
        @Override
        public QuotingStyle infix(@Nonnull CodePointPrinter out) {
            out.print(this.quote);
            return AUTO_QUOTE;
        }

    }

}
