package org.predicode.predicator.testutils

import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.creating.any.typetransformation.AnyTypeTransformation
import ch.tutteli.atrium.domain.robstoll.lib.creating.any.typetransformation.failurehandlers.ExplanatoryFailureHandler
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.translations.DescriptionBasic
import java.util.*

fun <T : Any> Assert<Optional<T>>.isEmpty() =
        createAndAddAssertion(DescriptionBasic.IS, RawString.create("empty")) { !subject.isPresent }

fun <T : Any> Assert<Optional<T>>.toContain(value: T) =
        createAndAddAssertion(DescriptionBasic.IS, value) { subject.map { it == value }.orElse(false) }

inline fun <reified T : Any> Assert<Optional<T>>.notToBeEmpty(
        noinline assertionCreator: Assert<T>.() -> Unit) {
    AssertImpl.any.typeTransformation.transform(
            AnyTypeTransformation.ParameterObject(
                    Untranslatable("not to be empty"),
                    "Optional::get",
                    this,
                    assertionCreator,
                    Untranslatable("optional is empty")),
            { it.isPresent },
            { it.get() },
            ExplanatoryFailureHandler())
}
