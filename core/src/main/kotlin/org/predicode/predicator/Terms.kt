@file:JvmName("Terms")

package org.predicode.predicator

/**
 * Creates a [keyword][Keyword] with the given name.
 *
 * This keyword matches only keywords constructed with this function with the same [name].
 *
 * @param name keyword name.
 */
fun namedKeyword(name: String): Keyword = NamedKeyword(name)

private class NamedKeyword(val name: String) : Keyword() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedKeyword

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "'$name'"

    override fun toPhraseString() = name

}

/**
 * Creates an [atom][Atom] with the given name.
 *
 * This atom matches another one only if the latter is constructed with this function and has the same [name].
 *
 * @param name atom name.
 */
fun namedAtom(name: String): Atom = NamedAtom(name)

private class NamedAtom(val name: String) : Atom() {

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

    override fun toString() = name

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

    override fun toString() = value.toString()

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

}

/**
 * Creates a named [variable][Variable].
 *
 * This variable matches another one only if the latter is constructed with this function with the same [name].
 *
 * @param name variable name.
 */
fun namedVariable(name: String): Variable = NamedVariable(name)

private class NamedVariable(val name: String) : Variable() {

    override fun toString(): String = "_${name}_"
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
