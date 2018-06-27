package org.predicode.predicator.grammar;

import org.predicode.predicator.terms.Term;

import javax.annotation.Nonnull;

import static org.predicode.predicator.grammar.CodePoints.*;
import static org.predicode.predicator.grammar.QuotingStyle.AUTO_QUOTE;
import static org.predicode.predicator.grammar.QuotingStyle.OPEN_QUOTE;
import static org.predicode.predicator.grammar.TermSep.*;


public class TermPrinter {

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
    private final CodePointPrinter out;

    @Nonnull
    private TermSep sep = NO_SEP;

    private TermPrinter(@Nonnull CodePointPrinter out) {
        this.out = out;
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

    public void keyword(@Nonnull CharSequence name, @Nonnull QuotedName quoted) {

        final QuotingStyle quoting;

        if (quoted.isInfix()) {
            quoting = this.sep.infix(this.out);
        } else {
            this.sep.keyword(this.out);
            quoting = AUTO_QUOTE;
        }

        quoting.printName(name, BACKTICK, this.out);
        this.sep = quoted.isPrefix() ? PREFIX_SEP : KEYWORD_SEP;
    }

    public void atom(@Nonnull CharSequence name) {
        quotedName(name, ATOM_SEP);
    }

    public void variable(@Nonnull CharSequence name) {
        quotedName(name, VARIABLE_SEP);
    }

    private void quotedName(@Nonnull CharSequence name, @Nonnull TermSep.QuotedSep sep) {
        this.sep.quoted(this.out);

        final boolean quoteClosed = OPEN_QUOTE.printName(name, sep.getQuote(), this.out);

        this.sep = quoteClosed ? SPACE_SEP : sep;
    }

    public void value(@Nonnull CharSequence value) {
        this.sep.value(this.out);
        this.out.print(OPENING_BRACE);
        this.out.print(value);
        this.out.print(CLOSING_BRACE);
        this.sep = SPACE_SEP;
    }

    public void special(@Nonnull CharSequence value) {
        this.sep.special(this.out);
        this.out.print(value);
        this.sep = SPECIAL_SEP;
    }

    public void startCompound() {
        this.sep.value(this.out);
        this.out.print(OPENING_PARENT);
        this.sep = NO_SEP;
    }

    public void endCompound() {
        this.out.print(CLOSING_PARENT);
        this.sep = SPACE_SEP;
    }

}
