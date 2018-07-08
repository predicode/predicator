package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;


@Immutable
final class NamedAtom extends Atom {

    NamedAtom(@Nonnull String name) {
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

        final NamedAtom that = (NamedAtom) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
