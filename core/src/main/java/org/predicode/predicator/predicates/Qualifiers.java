package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.*;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;


/**
 * A collection of qualifiers.
 *
 * <p>This is essentially a readonly map of qualifiers with their signatures as keys.</p>
 */
@Immutable
public final class Qualifiers extends AbstractCollection<Qualifier> {

    private static final Qualifiers NO_QUALIFIERS = new Qualifiers(emptyMap());

    @Nonnull
    public static Qualifiers noQualifiers() {
        return NO_QUALIFIERS;
    }

    public static Qualifiers oneQualifier(@Nonnull Qualifier qualifier) {
        return new Qualifiers(singletonMap(qualifier.getSignature(), qualifier));
    }

    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Qualifier... qualifiers) {
        return qualifiers(Stream.of(qualifiers));
    }

    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Collection<? extends Qualifier> qualifiers) {
        return qualifiers(qualifiers.stream());
    }

    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Iterable<? extends Qualifier> qualifiers) {
        return qualifiers(StreamSupport.stream(qualifiers.spliterator(), false));
    }

    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Stream<? extends Qualifier> qualifiers) {

        final Map<Qualifier.Signature, ? extends Qualifier> map =
                qualifiers.collect(Collectors.toMap(Qualifier::getSignature, UnaryOperator.identity()));

        if (map.isEmpty()) {
            return noQualifiers();
        }

        return new Qualifiers(unmodifiableMap(map));
    }

    @Nonnull
    private final Map<Qualifier.Signature, Qualifier> map;

    private Qualifiers(@Nonnull Map<Qualifier.Signature, Qualifier> map) {
        this.map = map;
    }

    /**
     * Qualifiers map.
     *
     * @return readonly map of qualifiers with their signatures as keys.
     */
    @Nonnull
    public final Map<? extends Qualifier.Signature, ? extends Qualifier> map() {
        return this.map;
    }

    /**
     * Returns qualifier with the given signature.
     *
     * @param signature target qualifier signature.
     *
     * @return an optional containing qualifier with the given {@code signature}, or empty optional if no such qualifier
     * present in this collection.
     */
    @Nonnull
    public final Optional<Qualifier> get(@Nonnull Qualifier.Signature signature) {
        return Optional.ofNullable(map().get(signature));
    }

    /**
     * Sets the given qualifiers.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers set(@Nonnull Qualifier... qualifiers) {
        return setAll(Arrays.asList(qualifiers), qualifiers.length);
    }

    /**
     * Sets the given qualifiers.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers a collection of qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers setAll(@Nonnull Collection<? extends Qualifier> qualifiers) {
        return setAll(qualifiers, qualifiers.size());
    }

    /**
     * Sets the given qualifiers.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers an iterable of qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers setAll(@Nonnull Iterable<? extends Qualifier> qualifiers) {
        return setAll(qualifiers, 1);
    }

    /**
     * Sets all the given qualifiers.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers setAll(@Nonnull Qualifiers qualifiers) {
        return setAll(qualifiers.map().values(), qualifiers.size());
    }

    /**
     * Sets the given qualifiers.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers a stream of qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers setAll(@Nonnull Stream<? extends Qualifier> qualifiers) {
        return setAll(qualifiers(qualifiers));
    }

    @Nonnull
    private Qualifiers setAll(@Nonnull Iterable<? extends Qualifier> qualifiers, int numQualifiers) {

        HashMap<Qualifier.Signature, Qualifier> newQualifiers = null;

        for (final Qualifier qualifier : qualifiers) {
            if (qualifier.equals(map().get(qualifier.getSignature()))) {
                continue;
            }
            if (newQualifiers == null) {
                newQualifiers = new HashMap<>(map().size() + numQualifiers);
                newQualifiers.putAll(map());
            }

            newQualifiers.put(qualifier.getSignature(), qualifier);
        }

        if (newQualifiers == null) {
            return this; // Qualifiers didn't change
        }

        return new Qualifiers(unmodifiableMap(newQualifiers));
    }

    /**
     * Attempts to match the given predicate qualifiers collection against this one.
     *
     * <p>This method is called for {@link Rule.Pattern#getQualifiers() rule pattern qualifiers} when
     * {@link Rule.Pattern#match(Predicate.Call, Knowns) matching} against {@link Predicate.Call#getQualifiers()
     * predicate call qualifiers}.</p>
     *
     * <p>Each qualifier in this pattern should match the corresponding one in the given qualifiers collection.</p>
     *
     * @param qualifiers a predicate qualifiers to match.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the qualifiers matches this pattern, or empty optional otherwise.
     */
    @Nonnull
    public final Optional<Knowns> match(@Nonnull Qualifiers qualifiers, @Nonnull Knowns knowns) {
        for (final Qualifier qualifier : this) {

            final Knowns k = knowns;
            final Optional<Knowns> match = qualifiers.get(qualifier.getSignature())
                    .flatMap(found -> qualifier.match(found, k));

            if (!match.isPresent()) {
                return Optional.empty();
            }

            knowns = match.get();
        }

        return Optional.of(knowns);
    }

    @Override
    public Iterator<Qualifier> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Qualifier)) {
            return false;
        }

        final Qualifier qualifier = (Qualifier) o;

        return qualifier.equals(this.map.get(qualifier.getSignature()));
    }

    /**
     * A size of this qualifiers collection.
     *
     * @return the number of qualifiers set.
     */
    @Override
    public final int size() {
        return this.map.size();
    }

    /**
     * Whether this collection is empty.
     *
     * @return {@code true} is this collection does not contain qualifiers, or {@code false} otherwise.
     */
    @Override
    public final boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public Spliterator<Qualifier> spliterator() {
        return this.map.values().spliterator();
    }

    @Override
    public Stream<Qualifier> stream() {
        return this.map.values().stream();
    }

    @Override
    public Stream<Qualifier> parallelStream() {
        return this.map.values().parallelStream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Qualifiers that = (Qualifiers) o;

        return this.map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {

        final StringBuilder out = new StringBuilder();

        printQualifiers(out);

        return out.toString();
    }

    /**
     * Appends a string representations of qualifiers to the given string builder.
     *
     * @param out string builder to append qualifiers to.
     */
    public void printQualifiers(@Nonnull StringBuilder out) {

        int i = 0;

        for (final Qualifier qualifier : this.map.values()) {
            if (i != 0) {
                out.append(' ');
            }
            out.append('@');
            ++i;
            printTerms(qualifier.getTerms(), out);
        }
    }

}
