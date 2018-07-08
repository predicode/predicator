package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;


@Immutable
final class TempVariable extends Variable {

    TempVariable(@Nonnull String name) {
        super(name);
    }

}
