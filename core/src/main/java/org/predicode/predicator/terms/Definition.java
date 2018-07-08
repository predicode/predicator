package org.predicode.predicator.terms;

import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;


class Definition extends Keyword {

    @Nonnull
    static final Definition INSTANCE = new Definition();

    private Definition() {
        super(":=");
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.special(getName());
    }

}
