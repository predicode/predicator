package org.predicode.predicator.grammar;

import org.predicode.predicator.Term;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.*;
import static org.predicode.predicator.grammar.PartPrinter.INITIAL_PART_PRINTER;
import static org.predicode.predicator.grammar.PartPrinter.UNQUOTED_PART_PRINTER;


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
        this.partPrinter = quotedName(name, SINGLE_QUOTE);
    }

    public void variable(@Nonnull CharSequence name) {
        this.partPrinter = quotedName(name, UNDERSCORE);
    }

    @Nonnull
    private PartPrinter quotedName(@Nonnull CharSequence name, int quote) {

        final PartPrinter it = this.partPrinter.separate(this);
        final boolean quoteClosed = QuotingStyle.OPEN_QUOTE.printName(name, quote, this.print);

        if (quoteClosed) {
            return UNQUOTED_PART_PRINTER;
        }

        return it.quoted(quote);
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
