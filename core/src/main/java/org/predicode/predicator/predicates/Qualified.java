package org.predicode.predicator.predicates;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;


/**
 * An entity with qualifiers.
 *
 * @param <T> qualified entity type.
 */
public interface Qualified<T extends Qualified<T>> {

    /**
     * Qualifiers of this instance.
     *
     * @return collection of qualifiers.
     */
    @Nonnull
    Qualifiers getQualifiers();

    /**
     * Updates qualifiers.
     *
     * @param updateQualifiers a function that receives current qualifiers and returns an updated ones.
     *
     * @return new instance with qualifiers updated by the given function, or this instance if qualifiers didn't change.
     */
    @Nonnull
    T qualify(@Nonnull UnaryOperator<Qualifiers> updateQualifiers);

    /**
     * Qualifies this instance.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers qualifiers to apply.
     *
     * @return new instance with the given qualifiers applied on top of this instance's ones,
     * or this instance if qualifiers didn't change.
     *
     * @see Qualifiers#set(Qualifier...)
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    default T qualify(@Nonnull Qualifier... qualifiers) {
        return qualify(old -> old.set(qualifiers));
    }

    /**
     * Qualifies this instance.
     *
     * <p>Either appends the given qualifiers, or updates the ones with the same signature.</p>
     *
     * @param qualifiers qualifiers to apply.
     *
     * @return new instance with the given qualifiers applied on top of this instance's ones,
     * or this instance if qualifiers didn't change.
     *
     * @see Qualifiers#setAll(Qualifiers)
     */
    @Nonnull
    default T qualify(@Nonnull Qualifiers qualifiers) {
        return qualify(old -> old.setAll(qualifiers));
    }

}
