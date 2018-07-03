package org.predicode.predicator.terms

/**
 * Creates a [keyword][Keyword] with the given name.
 *
 * @param name keyword name.
 *
 * @see Keyword.namedKeyword
 */
fun namedKeyword(name: String) = Keyword.namedKeyword(name)

/**
 * Creates an [atom][Atom] with the given name.
 *
 * @param name atom name.
 *
 * @see Atom.namedAtom
 */
fun namedAtom(name: String) = Atom.namedAtom(name)

/**
 * Creates a raw [value][Value].
 *
 * @param value target value.
 *
 * @see Value.rawValue
 */
fun <T> rawValue(value: T) = Value.rawValue(value)

/**
 * Creates a named [variable][Variable].
 *
 * @param name variable name.
 *
 * @see Variable.namedVariable
 */
fun namedVariable(name: String) = Variable.namedVariable(name)

/**
 * Creates temporary [variable][Variable].
 *
 * @param prefix temporary variable name prefix.
 *
 * @see Variable.tempVariable
 */
fun tempVariable(prefix: String) = Variable.tempVariable(prefix)
