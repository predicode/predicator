package org.predicode.predicator

/**
 * Creates a [keyword][Keyword] with the given name.
 *
 * @param name keyword name.
 *
 * @see Keyword.namedKeyword
 */
@Suppress("NOTHING_TO_INLINE")
inline fun namedKeyword(name: String) = Keyword.namedKeyword(name)

/**
 * Creates an [atom][Atom] with the given name.
 *
 * @param name atom name.
 *
 * @see Atom.namedAtom
 */
@Suppress("NOTHING_TO_INLINE")
inline fun namedAtom(name: String) = Atom.namedAtom(name)

/**
 * Creates a raw [value][Value].
 *
 * @param value target value.
 *
 * @see Value.rawValue
 */
@Suppress("NOTHING_TO_INLINE")
inline fun rawValue(value: Any) = Value.rawValue(value)

/**
 * Creates a named [variable][Variable].
 *
 * @param name variable name.
 *
 * @see Variable.namedVariable
 */
@Suppress("NOTHING_TO_INLINE")
inline fun namedVariable(name: String) = Variable.namedVariable(name)

/**
 * Creates temporary [variable][Variable].
 *
 * @param prefix temporary variable name prefix.
 *
 * @see Variable.tempVariable
 */
@Suppress("NOTHING_TO_INLINE")
inline fun tempVariable(prefix: String) = Variable.tempVariable(prefix)
