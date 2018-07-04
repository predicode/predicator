package org.predicode.predicator.predicates;

import org.predicode.predicator.Knowns;
import org.predicode.predicator.Rule;
import org.predicode.predicator.terms.Keyword;
import org.predicode.predicator.terms.Placeholder;
import org.predicode.predicator.terms.PlainTerm;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.*;
import static org.predicode.predicator.grammar.TermPrinter.printTerms;
import static org.predicode.predicator.terms.Variable.tempVariable;


/**
 * A collection of qualifiers.
 *
 * <p>This is essentially a readonly map of qualifiers with their signatures as keys.</p>
 */
@Immutable
public final class Qualifiers extends AbstractCollection<Qualifier> {

    private static final Qualifiers NO_QUALIFIERS = new Qualifiers(emptyMap());
    private static final Collector<Qualifier, ?, Qualifiers>
            TO_QUALIFIERS = Collectors.collectingAndThen(
                    Collectors.toMap(
                            Qualifier::getSignature,
                            UnaryOperator.identity(),
                            (q1, q2) -> q2),
                    map -> map.isEmpty() ? noQualifiers() : new Qualifiers(unmodifiableMap(map)));
    private static final ExtraQualifierBuilder EXTRA_QUALIFIER_TERM_REPLACER =
            new ExtraQualifierBuilder();

    /**
     * Empty qualifiers collection.
     *
     * @return a collection not containing any qualifiers.
     */
    @Nonnull
    public static Qualifiers noQualifiers() {
        return NO_QUALIFIERS;
    }

    /**
     * Creates a collection consisting of one qualifier.
     *
     * @param qualifier a qualifier to build a collection of.
     *
     * @return new qualifiers collection.
     */
    @Nonnull
    public static Qualifiers oneQualifier(@Nonnull Qualifier qualifier) {
        return new Qualifiers(singletonMap(qualifier.getSignature(), qualifier));
    }

    /**
     * Creates qualifiers collection.
     *
     * @param qualifiers qualifiers of the target collection.
     *
     * @return new qualifiers collection.
     */
    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Qualifier... qualifiers) {
        if (qualifiers.length == 1) {
            return oneQualifier(qualifiers[0]);
        }
        return Stream.of(qualifiers).collect(toQualifiers());
    }

    /**
     * Creates qualifiers collection out of collection of qualifiers.
     *
     * @param qualifiers a collection of qualifiers.
     *
     * @return new qualifiers collection.
     */
    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Collection<? extends Qualifier> qualifiers) {
        return qualifiers.stream().collect(toQualifiers());
    }

    /**
     * Creates qualifiers collection out of iterable.
     *
     * @param qualifiers an iterable of qualifiers.
     *
     * @return new qualifiers collection.
     */
    @Nonnull
    public static Qualifiers qualifiers(@Nonnull Iterable<? extends Qualifier> qualifiers) {
        return StreamSupport.stream(qualifiers.spliterator(), false).collect(toQualifiers());
    }

    @Nonnull
    public static Collector<Qualifier, ?, Qualifiers> toQualifiers() {
        return TO_QUALIFIERS;
    }

    @Nonnull
    private final Map<Qualifier.Signature, Qualifier> map;

    Qualifiers(@Nonnull Map<Qualifier.Signature, Qualifier> map) {
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
        return setAll(Stream.of(qualifiers), qualifiers.length);
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
        return setAll(qualifiers.stream(), qualifiers.size());
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
        return setAll(StreamSupport.stream(qualifiers.spliterator(), false), 1);
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
        return setAll(qualifiers.stream(), qualifiers.size());
    }

    @Nonnull
    private Qualifiers setAll(@Nonnull Stream<? extends Qualifier> qualifiers, int numQualifiers) {
        if (numQualifiers == 0) {
            return this;
        }
        return update(
                qualifiers.filter(qualifier -> !qualifier.equals(map().get(qualifier.getSignature()))),
                () -> {

                    final HashMap<Qualifier.Signature, Qualifier> newQualifiers =
                            new HashMap<>(map().size() + numQualifiers);

                    newQualifiers.putAll(map());

                    return newQualifiers;
                },
                this);
    }

    /**
     * Fulfill qualifiers.
     *
     * <p>Sets each of the given qualifiers, unless the qualifier with the same signature already present in this
     * collection.</p>
     *
     * @param qualifiers qualifiers to set.
     *
     * @return new qualifiers collection with the given qualifiers set on top of this ones,
     * or this instance if qualifiers didn't change.
     */
    @Nonnull
    public final Qualifiers fulfill(@Nonnull Qualifiers qualifiers) {
        if (qualifiers.isEmpty()) {
            return this;
        }
        return update(
                qualifiers.stream().filter(qualifier -> !map().containsKey(qualifier.getSignature())),
                () -> {

                    final HashMap<Qualifier.Signature, Qualifier> newQualifiers =
                            new HashMap<>(map().size() + qualifiers.size());

                    newQualifiers.putAll(map());

                    return newQualifiers;
                },
                this);
    }

    /**
     * Excludes qualifiers with the given signatures.
     *
     * <p>Construct qualifiers collection containing this one's qualifiers except the ones with signatures present
     * in the given collection.</p>
     *
     * @param qualifiers qualifiers collection to exclude.
     *
     * @return new qualifiers collection, or this instance if not modified.
     */
    public final Qualifiers exclude(@Nonnull Qualifiers qualifiers) {
        if (isEmpty() || qualifiers.isEmpty()) {
            return this;
        }

        final Qualifiers result = qualifiers.update(
                stream().filter(qualifier -> !qualifiers.map().containsKey(qualifier.getSignature())),
                () -> new HashMap<>(size() - qualifiers.size()),
                noQualifiers());

        return result.size() == size() ? this : result;
    }

    @Nonnull
    private Qualifiers update(
            @Nonnull Stream<? extends Qualifier> qualifiers,
            @Nonnull Supplier<HashMap<Qualifier.Signature, Qualifier>> createMap,
            @Nonnull Qualifiers defaultResult) {
        return qualifiers.collect(new QualifiersCollector(createMap, defaultResult));
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

            final Knowns $knowns = knowns;
            final Optional<Knowns> match = qualifiers.get(qualifier.getSignature())
                    .flatMap(found -> qualifier.match(found, $knowns));

            if (!match.isPresent()) {
                return Optional.empty();
            }

            knowns = match.get();
        }

        return Optional.of(qualifiers.exclude(this).addAsExtraQualifiersTo(knowns));
    }

    @Nonnull
    private Knowns addAsExtraQualifiersTo(@Nonnull Knowns knowns) {
        if (isEmpty()) {
            return knowns;
        }

        final ArrayList<Qualifier> qualifiers = new ArrayList<>(size());

        for (final Qualifier qualifier : this) {
            knowns = addExtraQualifierTo(qualifier, knowns, qualifiers);
        }

        return knowns.attr(Qualifiers.class, qualifiers(qualifiers));
    }

    @Nonnull
    private static Knowns addExtraQualifierTo(
            @Nonnull Qualifier qualifier,
            @Nonnull Knowns knowns,
            @Nonnull ArrayList<Qualifier> qualifiers) {

        final ArrayList<PlainTerm> terms = new ArrayList<>(qualifier.getTerms().size());

        for (final PlainTerm term : qualifier.getTerms()) {

            final Tuple2<PlainTerm, Knowns> tuple = term.accept(EXTRA_QUALIFIER_TERM_REPLACER, knowns);
            final PlainTerm replacement = tuple.getT1();

            terms.add(replacement);
            knowns = replacement.match(term, tuple.getT2()).orElseGet(() -> {
                throw new IllegalStateException(
                        "Can not find a replacement for extra qualifier term (" + term + ") of " + qualifier);
            });
        }

        qualifiers.add(new Qualifier(terms));

        return knowns;
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

    private static class ExtraQualifierBuilder implements PlainTerm.Visitor<Knowns, Tuple2<PlainTerm, Knowns>> {

        @Nonnull
        @Override
        public Tuple2<PlainTerm, Knowns> visitKeyword(
                @Nonnull Keyword keyword,
                @Nonnull Knowns knowns) {
            return Tuples.of(keyword, knowns);
        }

        @Nonnull
        @Override
        public Tuple2<PlainTerm, Knowns> visitPlaceholder(
                @Nonnull Placeholder placeholder,
                @Nonnull Knowns knowns) {
            return Tuples.of(placeholder, knowns);
        }

        @Nonnull
        @Override
        public Tuple2<PlainTerm, Knowns> visitPlain(
                @Nonnull PlainTerm term,
                @Nonnull Knowns knowns) {
            return knowns.declareLocal(tempVariable("extra qualifier"), Tuples::of);
        }

    }

}
