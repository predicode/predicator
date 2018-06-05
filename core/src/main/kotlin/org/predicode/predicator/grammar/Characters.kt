@file:JvmName("Characters")
package org.predicode.predicator.grammar

typealias CodePoint = Int

typealias CodePointConsumer = (CodePoint) -> Unit

const val BACKTICK: CodePoint = '`'.toInt()
const val SINGLE_QUOTE: CodePoint = '\''.toInt()
const val DOUBLE_QUOTE: CodePoint = '"'.toInt()
const val BACKSLASH: CodePoint = '\\'.toInt()
const val SPACE: CodePoint = ' '.toInt()
const val UNDERSCORE: CodePoint = '_'.toInt()
const val OPENING_BRACE: CodePoint = '['.toInt()
const val CLOSING_BRACE: CodePoint = ']'.toInt()
const val OPENING_PARENT: CodePoint = '('.toInt()
const val CLOSING_PARENT: CodePoint = ')'.toInt()

@Suppress("NOTHING_TO_INLINE")
inline fun Char.toCodePoint(): CodePoint = toInt()
