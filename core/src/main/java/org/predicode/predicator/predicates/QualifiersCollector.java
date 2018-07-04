package org.predicode.predicator.predicates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;


final class QualifiersCollector implements Collector<Qualifier, QualifiersCollector, Qualifiers> {

    @Nonnull
    final Supplier<HashMap<Qualifier.Signature, Qualifier>> createMap;

    @Nonnull
    private final Qualifiers defaultQualifiers;

    @Nullable
    private HashMap<Qualifier.Signature, Qualifier> map;

    QualifiersCollector(
            @Nonnull Supplier<HashMap<Qualifier.Signature, Qualifier>> createMap,
            @Nonnull Qualifiers defaultQualifiers) {
        this.createMap = createMap;
        this.defaultQualifiers = defaultQualifiers;
    }

    @Override
    public Supplier<QualifiersCollector> supplier() {
        return () -> this;
    }

    @Override
    public BiConsumer<QualifiersCollector, Qualifier> accumulator() {
        return QualifiersCollector::set;
    }

    private void set(@Nonnull Qualifier qualifier) {
        if (this.map == null) {
            this.map = this.createMap.get();
        }
        this.map.put(qualifier.getSignature(), qualifier);
    }

    @Nonnull
    private final HashMap<Qualifier.Signature, Qualifier> map() {
        if (this.map != null) {
            return this.map;
        }
        return this.map = this.createMap.get();
    }

    @Override
    public BinaryOperator<QualifiersCollector> combiner() {
        return QualifiersCollector::merge;
    }

    @Nonnull
    private QualifiersCollector merge(@Nonnull QualifiersCollector other) {
        if (this.map == null) {
            return other;
        }
        if (other.map == null) {
            return this;
        }
        this.map.putAll(other.map);
        return this;
    }

    @Override
    public Function<QualifiersCollector, Qualifiers> finisher() {
        return QualifiersCollector::build;
    }

    @Nonnull
    private Qualifiers build() {
        if (this.map == null) {
            return this.defaultQualifiers;
        }
        return new Qualifiers(unmodifiableMap(this.map));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return emptySet();
    }

}
