package org.predicode.predicator.grammar;

import org.predicode.predicator.terms.Term;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.*;
import static org.predicode.predicator.grammar.PartPrinter.*;
import static org.predicode.predicator.grammar.QuotingStyle.OPEN_QUOTE;


public class TermPrinter implements CodePointPrinter {

    public static void printTerms(@Nonnull Iterable<? extends Term> terms, @Nonnull CodePointPrinter print) {
        new TermPrinter(print).print(terms);
    }

    @Nonnull
    public static String printTerms(@Nonnull Iterable<? extends Term> terms) {

        final StringBuilder out = new StringBuilder();

        printTerms(terms, out::appendCodePoint);

        return out.toString();
    }

    public static void printTerms(@Nonnull CodePointPrinter print, @Nonnull Term ...terms) {
        new TermPrinter(print).print(terms);
    }

    @Nonnull
    public static String printTerms(@Nonnull Term ...terms) {

        final StringBuilder out = new StringBuilder();

        printTerms(out::appendCodePoint, terms);

        return out.toString();
    }

    @Nonnull
    private final CodePointPrinter print;

    @Nonnull
    private PartPrinter partPrinter = INITIAL_PART_PRINTER;

    private TermPrinter(@Nonnull CodePointPrinter print) {
        this.print = print;
    }

    @Override
    public void print(int codePoint) {
        this.print.print(codePoint);
    }

    public void print(@Nonnull Iterable<? extends Term> terms) {
        for (Term term : terms) {
            term.print(this);
        }
    }

    public void print(@Nonnull Term ...terms) {
        for (Term term : terms) {
            term.print(this);
        }
    }

    public void keyword(@Nonnull CharSequence name) {
        this.partPrinter = this.partPrinter.keyword(this, name);
    }

    public void atom(@Nonnull CharSequence name) {
        quotedName(name, ATOM_PART_PRINTER);
    }

    public void variable(@Nonnull CharSequence name) {
        quotedName(name, VARIABLE_PART_PRINTER);
    }

    private void quotedName(@Nonnull CharSequence name, @Nonnull PartPrinter.QuotedPartPrinter quoted) {
        this.partPrinter.separate(this);

        final boolean quoteClosed = OPEN_QUOTE.printName(name, quoted.getQuote(), this.print);

        if (quoteClosed) {
            this.partPrinter = UNQUOTED_PART_PRINTER;
        } else {
            this.partPrinter = quoted;
        }
    }

    public void value(@Nonnull CharSequence value) {
        this.partPrinter = this.partPrinter.separate(this);
        print(OPENING_BRACE);
        print(value);
        print(CLOSING_BRACE);
    }

    public void startCompound() {
        this.partPrinter.endQuoted(this).separate(this);
        print(OPENING_PARENT);
        this.partPrinter = INITIAL_PART_PRINTER;
    }

    public void endCompound() {
        this.partPrinter = this.partPrinter.endQuoted(this);
        print(CLOSING_PARENT);
    }

}
