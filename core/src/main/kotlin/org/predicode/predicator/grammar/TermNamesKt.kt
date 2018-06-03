@file:JvmName("TermNamesKt")
package org.predicode.predicator.grammar

import java.util.function.IntConsumer

fun printName(
        name: CharSequence,
        quote: CodePoint,
        quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE,
        print: (CodePoint) -> Unit) = printName(
        name,
        print = IntConsumer(print),
        quote = quote,
        quoting = quoting)
