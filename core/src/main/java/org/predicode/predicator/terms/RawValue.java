package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Optional;


@Immutable
final class RawValue<T> extends Value<T> {

    @Nonnull
    private final T value;

    RawValue(@Nonnull T value) {
        this.value = value;
    }

    /**
     * Returns the value contents.
     *
     * @return the contents of this value.
     */
    @Nonnull
    @Override
    public T get() {
        return this.value;
    }

    @Nonnull
    @Override
    protected Optional<Knowns> valueMatch(@Nonnull Value<?> other, @Nonnull Knowns knowns) {
        return equals(other) ? Optional.of(knowns) : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RawValue<?> rawValue = (RawValue<?>) o;

        return this.value.equals(rawValue.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

}
