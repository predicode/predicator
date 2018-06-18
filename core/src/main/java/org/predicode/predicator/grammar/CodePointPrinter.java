package org.predicode.predicator.grammar;

import org.predicode.predicator.annotations.SamWithReceiver;

import javax.annotation.Nonnull;


@FunctionalInterface
@SamWithReceiver
public interface CodePointPrinter {

    void print(int codePoint);

    default void print(@Nonnull CharSequence text) {
        text.codePoints().forEach(this::print);
    }

}
