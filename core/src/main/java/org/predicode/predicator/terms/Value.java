package org.predicode.predicator.terms;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.grammar.TermPrinter;

import javax.annotation.Nonnull;
import java.util.Optional;


/**
 * Arbitrary value term.
 *
 * <p>Values match only {@link #valueMatch(Value, Knowns) matching values} and can be mapped to variables.</p>
 */
public abstract class Value<T> extends ResolvedTerm {

    /**
     * Creates a raw value.
     *
     * This value matches another one only if the latter is constructed with this function with equal {@code value}.
     *
     * @param value target value.
     */
    @Nonnull
    public static <T> Value<T> rawValue(@Nonnull T value) {
        return new RawValue<>(value);
    }

    @Nonnull
    @Override
    public Optional<Knowns> match(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {

        final Value<?> self = this;

        return term.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitValue(@Nonnull Value<?> value, @Nonnull Knowns knowns) {
                        return value.valueMatch(self, knowns);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitVariable(@Nonnull Variable variable, @Nonnull Knowns knowns) {
                        return knowns.resolve(variable, self);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitPlaceholder(@Nonnull Placeholder placeholder, @Nonnull Knowns knowns) {
                        return Optional.of(knowns);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitPlain(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
                        return Optional.empty();
                    }

                },
                knowns);
    }

    @Nonnull
    public abstract T get();

    @Nonnull
    @Override
    public final <P, R> R accept(@Nonnull ResolvedTerm.Visitor<P, R> visitor, @Nonnull P p) {
        return visitor.visitValue(this, p);
    }

    @Override
    public void print(@Nonnull TermPrinter out) {
        out.value(toString());
    }

    /**
     * Attempts to match against another value.
     *
     * <p>This method is called from {@link #match(PlainTerm, Knowns)} one when the term to match is a value.</p>
     *
     * @param other another value to match against.
     * @param knowns known resolutions to update.
     *
     * @return updated knowns if the term matches, or empty optional otherwise.
     */
    @Nonnull
    protected abstract Optional<Knowns> valueMatch(@Nonnull Value<?> other, @Nonnull Knowns knowns);

    private static final class RawValue<T> extends Value<T> {

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

}
