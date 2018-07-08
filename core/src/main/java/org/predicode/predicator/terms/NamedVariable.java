package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;


@Immutable
final class NamedVariable extends Variable {

    NamedVariable(@Nonnull String name) {
        super(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final NamedVariable that = (NamedVariable) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
