package org.predicode.predicator;

import org.predicode.predicator.predicates.Predicate;
import org.predicode.predicator.terms.MappedTerm;
import org.predicode.predicator.terms.PlainTerm;
import org.predicode.predicator.terms.ResolvedTerm;
import org.predicode.predicator.terms.Variable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;


public class Knowns {

    private static final PlainTerm.Visitor<Knowns, Resolution> ENSURE_QUERY_VARIABLE_EXISTS =
            new PlainTerm.Visitor<Knowns, Resolution>() {

        @Nonnull
        @Override
        public Resolution visitVariable(@Nonnull Variable variable, @Nonnull Knowns knowns) {
            return knowns.resolution(variable);
        }

        @Nonnull
        @Override
        public Resolution visitPlain(@Nonnull PlainTerm term, @Nonnull Knowns knowns) {
            return UNRESOLVED;
        }

    };

    /**
     * Original query variable resolutions.
     *
     * <p>Once query variable is resolved its resolution can not change any more.</p>
     *
     * <p>Query variable names are set only once via constructor. The list of query variables can not change after that.
     * </p>
     */
    @Nonnull
    private final Map<Variable, Resolution> resolutions;

    /**
     * Resolution rule variable mappings.
     *
     * <p>These are {@link PlainTerm plain terms} passed to resolution rule. I.e. variable values local to the rule.
     * When {@link Variable variable} is used as local variable value, it is the one from {@link #resolutions original
     * query}.</p>
     */
    @Nonnull
    private final Map<Variable, MappedTerm> mappings;

    /**
     * Resolution attributes.
     *
     * <p>These attributes are wiped out when {@link #startMatching() new rule match is started}.</p>
     */
    @Nonnull
    private final Map<Class<?>, Object> attrs;

    private int rev;

    /**
     * Constructs knowns without any mappings and with the given query variables unresolved.
     *
     * @param variables query variables.
     */
    public Knowns(@Nonnull Variable ...variables) {
        this.mappings = emptyMap();
        this.resolutions = Stream.of(variables)
                .collect(Collectors.toMap(UnaryOperator.identity(), v -> UNRESOLVED));
        this.attrs = emptyMap();
    }

    private Knowns(
            @Nonnull Knowns proto,
            @Nonnull Map<Variable, Resolution> resolutions,
            @Nonnull Map<Variable, MappedTerm> mappings) {
        this.resolutions = resolutions;
        this.mappings = mappings;
        this.attrs = proto.attrs;
        this.rev = proto.rev;
    }

    private Knowns(
            @Nonnull Knowns proto,
            @Nonnull Map<Class<?>, Object> attrs) {
        this.resolutions = proto.resolutions;
        this.mappings = proto.mappings;
        this.attrs = attrs;
        this.rev = proto.rev;
    }

    private Knowns(@Nonnull Knowns proto) {
        this.resolutions = proto.resolutions;
        this.mappings = proto.mappings;
        this.attrs = emptyMap();
        this.rev = proto.rev + 1;
    }

    /**
     * Handles the given local variable mapping.
     *
     * <p>If the given variable is not mapped yet, then declares a local variable and maps the given variable to it.
     * The updated knowns are passed to {@code handler}.</p>
     *
     * @param variable a variable, local to resolution rule.
     * @param handler a handler function accepting mapping and updated knowns as argument and returning arbitrary value.
     */
    @Nonnull
    public <R> Optional<R> mapping(
            @Nonnull Variable variable,
            @Nonnull BiFunction<? super PlainTerm, ? super Knowns, ? extends R> handler) {

        final PlainTerm mapping = this.mappings.get(variable);

        if (mapping != null) {
            return Optional.of(handler.apply(mapping, this));
        }

        return Optional.of(declareLocal(variable, handler));
    }

    /**
     * Handles the given local variable mapping.
     *
     * </p>If the given variable is not mapped yet, then declares a local variable and maps the given variable to it.
     * The updated knowns are passed to {@code handler}.</p>
     *
     * @param variable a variable, local to resolution rule.
     * @param handler a handler function accepting mapping and updated knowns as argument and returning arbitrary value.
     */
    public <R> R declareLocal(
            @Nonnull Variable variable,
            @Nonnull BiFunction<? super Variable, ? super Knowns, ? extends R> handler) {

        final LocalVariable local = new LocalVariable(variable, this.rev);
        final Knowns knowns = declareLocal(local);

       return handler.apply(local, knowns);
    }

    @Nonnull
    private Knowns declareLocal(@Nonnull LocalVariable local) {
        if (this.resolutions.containsKey(local)) {
            return this;
        }

        final HashMap<Variable, Resolution> resolutions = new HashMap<>(this.resolutions.size() + 1);

        resolutions.putAll(this.resolutions);
        resolutions.put(local, UNRESOLVED);

        final HashMap<Variable, MappedTerm> mappings = new HashMap<>(this.mappings.size() + 1);

        mappings.putAll(this.mappings);
        mappings.put(local.variable, local);

        return new Knowns(this, resolutions, mappings);
    }

    /**
     * Maps local resolution rule variable to the new value.
     *
     * If the value already set it can not be updated, unless the value is a query variable. In the latter case the
     * target variable is resolved to previous value.
     *
     * @param variable variable local to resolution rule.
     * @param value new variable value.
     *
     * @return updated resolutions, or `null` if they can not be updated thus making corresponding rule effectively
     * unmatched.
     */
    @Nonnull
    public Optional<Knowns> map(@Nonnull Variable variable, @Nonnull MappedTerm value) {

        final MappedTerm prev = this.mappings.get(variable);

        if (prev == null) {
            // New mapping
            value.accept(ENSURE_QUERY_VARIABLE_EXISTS, this);

            final HashMap<Variable, MappedTerm> mappings = new HashMap<>(this.mappings.size() + 1);

            mappings.putAll(this.mappings);
            mappings.put(variable, value);

            return Optional.of(new Knowns(
                    this,
                    this.resolutions,
                    mappings));
        }
        if (prev.equals(value)) {
            // Mapping didn't change
            return Optional.of(this);
        }

        return updateMapping(value, prev);
    }

    @Nonnull
    private Optional<Knowns> updateMapping(@Nonnull MappedTerm value, @Nonnull MappedTerm prev) {
        return prev.accept(
                new MappedTerm.Visitor<MappedTerm, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitVariable(@Nonnull Variable variable, @Nonnull MappedTerm value) {
                        return resolve(variable, value);
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitMapped(@Nonnull MappedTerm term, @Nonnull MappedTerm value) {
                        return Optional.empty();
                    }

                },
                value);
    }

    @Nonnull
    private Optional<Knowns> resolve(@Nonnull Variable variable, @Nonnull MappedTerm value) {

        final Resolution resolution = resolution(variable);

        return resolution.value()
                .<Optional<Knowns>>map(oldValue ->
                        oldValue.equals(value)
                                ? Optional.of(this)
                                : Optional.empty())
                .orElseGet(() -> resolution.aliased()
                        .map(aliased -> resolve(aliased, value))
                        .orElseGet(() -> addResolution(variable, value)));
    }

    @Nonnull
    private Optional<Knowns> addResolution(@Nonnull Variable var, @Nonnull MappedTerm value) {
        return value.accept(
                new PlainTerm.Visitor<Knowns, Optional<Knowns>>() {

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitResolved(
                            @Nonnull ResolvedTerm term,
                            @Nonnull Knowns knowns) {

                        final HashMap<Variable, Resolution> resolutions =
                                new HashMap<>(knowns.resolutions.size() + 1);

                        resolutions.putAll(knowns.resolutions);
                        resolutions.put(var, new Resolved(term));

                        return Optional.of(new Knowns(knowns, resolutions, knowns.mappings));
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitVariable(
                            @Nonnull Variable variable,
                            @Nonnull Knowns knowns) {
                        knowns.resolution(variable); // Ensure aliased query variable exists

                        final HashMap<Variable, Resolution> resolutions =
                                new HashMap<>(knowns.resolutions.size() + 1);

                        resolutions.putAll(knowns.resolutions);
                        resolutions.put(var, new Alias(variable));

                        return Optional.of(new Knowns(knowns, resolutions, knowns.mappings));
                    }

                    @Nonnull
                    @Override
                    public Optional<Knowns> visitPlain(
                            @Nonnull PlainTerm term,
                            @Nonnull Knowns knowns) {
                        return Optional.empty();
                    }

                },
                this);
    }

    /**
     * Creates knowns to update while matching the rules.
     *
     * <p>This is called in the very beginning of {@link Rule#getCondition() rule condition}
     * {@link Rule.Pattern#match(Predicate.Call, Knowns) match}.</p>
     *
     * <p>This wipes out {@link #attr(Class) resolution attributes}. Also, the
     * {@link #declareLocal(Variable, BiFunction) locals declared} after this call are unique and does not correspond
     * to the locals declared earlier.</p>
     */
    @Nonnull
    public final Knowns startMatching() {
        return new Knowns(this);
    }

    /**
     * Returns the given query variable resolution.
     *
     * @param variable query variable.
     *
     * @throws UnknownVariableException if there is no such variable in original query.
     */
    @Nonnull
    public Resolution resolution(@Nonnull Variable variable) {
        return this.resolutions.computeIfAbsent(
                variable,
                v -> {
                    throw new UnknownVariableException(v);
                });
    }

    /**
     * Resolves original query variable.
     *
     * <p>It is an error attempting to resolve non-existing variable.</p>
     *
     * @param variable original query variable.
     */
    @Nonnull
    public Optional<Knowns> resolve(@Nonnull Variable variable, @Nonnull ResolvedTerm value) {

        final Resolution resolution = resolution(variable);

        return resolution.value()
                .<Optional<Knowns>>map(oldValue ->
                        oldValue.equals(value) // Resolution can not change
                                ? Optional.of(this)
                                : Optional.empty())
                .orElseGet(() -> resolution.aliased()
                        .map(aliased -> resolve(aliased, value)) // Resolve aliased variable
                        .orElseGet(() -> {
                            // Resolve

                            final HashMap<Variable, Resolution> resolutions =
                                    new HashMap<>(this.resolutions.size() + 1);

                            resolutions.putAll(this.resolutions);
                            resolutions.put(variable, new Resolved(value));

                            return Optional.of(new Knowns(this, resolutions, this.mappings));
                        }));
    }

    /**
     * Returns resolution attribute with the given type.
     *
     * @param type a class of resolution attribute.
     * @param <T> as type of resolution attribute.
     *
     * @return optional containing resolution attribute value, or empty optional if there is no such attribute exists.
     */
    @Nonnull
    public final <T> Optional<T> attr(@Nonnull Class<? extends T> type) {

        @SuppressWarnings("unchecked")
        final T value = (T) this.attrs.get(type);

        return Optional.ofNullable(value);
    }

    /**
     * Assigns resolution attribute value.
     *
     * <p>Resolution attributes are identified by their types. At most one resolution attribute with the given type
     * exists.</p>
     *
     * <p>Resolution attributes are typically assigned during rule pattern match and used by predicates. They are wiped
     * out when {@link #startMatching() new rule match is started}.</p>
     *
     * @param type a class of resolution attribute.
     * @param value new resolution attribute value.
     * @param <T> a type of resolution attribute.
     *
     * @return a new knowns instance with the given attribute set to the specified value.
     */
    @Nonnull
    public final <T> Knowns attr(@Nonnull Class<? extends T> type, @Nonnull T value) {

        final HashMap<Class<?>, Object> attrs = new HashMap<>(this.attrs.size() + 1);

        attrs.putAll(this.attrs);
        attrs.put(type, value);

        return new Knowns(this, attrs);
    }

    private static final Resolution UNRESOLVED = new Resolution() {

        @Nonnull
        @Override
        public Optional<ResolvedTerm> value() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<Variable> aliased() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Unresolved";
        }

    };

    /**
     * Query variable resolution.
     */
    public static abstract class Resolution {

        private Resolution() {
        }

        /**
         * Whether the query variable is resolved.
         */
        public final boolean isResolved() {
            return this != UNRESOLVED;
        }

        /**
         * A term the query variable is resolved to.
         */
        @Nonnull
        public abstract Optional<ResolvedTerm> value();

        /**
         * Another variable the query one is alias for.
         */
        @Nonnull
        public abstract Optional<Variable> aliased();

    }

    private static final class Resolved extends Resolution {

        @Nonnull
        private final ResolvedTerm value;

        Resolved(@Nonnull ResolvedTerm value) {
            this.value = value;
        }

        @Nonnull
        @Override
        public Optional<ResolvedTerm> value() {
            return Optional.of(this.value);
        }

        @Nonnull
        @Override
        public Optional<Variable> aliased() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Resolved(" + this.value + ')';
        }

    }

    private static final class Alias extends Resolution {

        @Nonnull
        private final Variable aliased;

        Alias(@Nonnull Variable aliased) {
            this.aliased = aliased;
        }

        @Nonnull
        @Override
        public Optional<ResolvedTerm> value() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<Variable> aliased() {
            return Optional.of(this.aliased);
        }

        @Override
        public String toString() {
            return "Alias(" + this.aliased + ')';
        }

    }

    private static final class LocalVariable extends Variable {

        @Nonnull
        private final Variable variable;

        private final int rev;

        LocalVariable(@Nonnull Variable variable, int rev) {
            super(variable.getName() + " #" + rev);
            this.variable = variable;
            this.rev = rev;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LocalVariable that = (LocalVariable) o;

            if (this.rev != that.rev) {
                return false;
            }

            return this.variable.equals(that.variable);
        }

        @Override
        public int hashCode() {

            int result = this.variable.hashCode();

            result = 31 * result + this.rev;

            return result;
        }

    }

}
