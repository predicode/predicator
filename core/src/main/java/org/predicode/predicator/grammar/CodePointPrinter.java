package org.predicode.predicator.grammar;

import javax.annotation.Nonnull;


@FunctionalInterface
public interface CodePointPrinter {

    void print(int codePoint);

    default void print(@Nonnull CharSequence text) {
        text.codePoints().forEach(this::print);
    }

}
