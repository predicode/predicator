package org.predicode.predicator.terms;

import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;


@Immutable
final class NamedKeyword extends Keyword {

    NamedKeyword(@Nonnull String name, @Nonnull Kind kind) {
        super(name, kind);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final NamedKeyword operator = (NamedKeyword) o;

        if (!getName().equals(operator.getName())) {
            return false;
        }

        return getKind() == operator.getKind();
    }

    @Override
    public int hashCode() {

        int result = getName().hashCode();

        result = 31 * result + getKind().hashCode();

        return result;
    }

}
