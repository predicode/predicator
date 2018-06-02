@file:JvmName("Terms")
package org.predicode.predicator

import java.util.*

/**
 * Creates a [keyword][Keyword] with the given name.
 *
 * This keyword matches only keywords constructed with this function with the same [name].
 *
 * @param name keyword name.
 */
fun namedKeyword(name: String): Keyword = NamedKeyword(name)

private class NamedKeyword(override val name: String) : Keyword() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedKeyword

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()

}

/**
 * A keyword designating definition of expression.
 *
 * This is used to build phrase expansion rules. When expanding a phrase, it is replaced by (temporary) variable,
 * while predicate constructed to find a definition rule.
 *
 * A definition rule pattern consists of a variable, followed by this keyword, followed by expression terms.
 */
fun definitionKeyword(): Keyword = DefinitionKeyword

private object DefinitionKeyword : Keyword() {

    override val name: String
        get() = ":="

}

/**
 * Creates an [atom][Atom] with the given name.
 *
 * This atom matches another one only if the latter is constructed with this function and has the same [name].
 *
 * @param name atom name.
 */
fun namedAtom(name: String): Atom = NamedAtom(name)

private class NamedAtom(override val name: String) : Atom() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedAtom

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

/**
 * Creates a raw [value][Value].
 *
 * This value matches another one only if the latter is constructed with this function with equal [value].
 *
 * @param value target value.
 */
fun rawValue(value: Any): Value = RawValue(value)

private class RawValue<out V>(val value: V) : Value() {

    override fun valueMatch(other: Value, knowns: Knowns) =
            knowns.takeIf { this == other }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawValue<*>

        return value == other.value

    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString() = value.toString()

}

/**
 * Creates a named [variable][Variable].
 *
 * This variable matches another one only if the latter is constructed with this function with the same [name].
 *
 * @param name variable name.
 */
fun namedVariable(name: String): Variable = NamedVariable(name)

private class NamedVariable(override val name: String) : Variable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedVariable

        return name == other.name

    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

/**
 * Create temporary [variable][Variable].
 *
 * Temporary variables are compared by their identity. In contrast to [named variables][namedVariable] the name of
 * temporary one is used only for its representation.
 *
 * @param prefix temporary variable name prefix.
 */
fun tempVariable(prefix: String): Variable = TempVariable(prefix)

private val tempNameRandom = Random()

private class TempVariable(prefix: String) : Variable() {

    override val name = "$prefix ${tempNameRandom.nextInt()}"

}
