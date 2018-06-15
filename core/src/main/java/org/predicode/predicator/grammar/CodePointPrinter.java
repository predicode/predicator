package org.predicode.predicator.grammar;

import org.predicode.predicator.annotations.SamWithReceiver;


@FunctionalInterface
@SamWithReceiver
public interface CodePointPrinter {

    void print(int codePoint);

}
