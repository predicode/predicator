package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QualifiersTest {

    @Test
    fun `construction does not depend on qualifiers order`() {
        assertThat(Qualifiers.qualifiers(testQualifier(), testQualifier2()))
                .toBe(Qualifiers.qualifiers(testQualifier2(), testQualifier()))
    }

    @Test
    fun `contains qualifiers`() {

        val qualifiers = Qualifiers.qualifiers(testQualifier(), testQualifier2())

        assertThat(qualifiers)
                .contains(testQualifier(), testQualifier2())
                .hasSize(2)
    }

    @Test
    fun `does not duplicate qualifiers`() {

        val qualifiers = Qualifiers.qualifiers(testQualifier(), testQualifier())

        assertThat(qualifiers).containsStrictly(testQualifier())
    }

    @Test
    fun `build singleton on empty input`() {
        assertThat(Qualifiers.qualifiers()).isSame(Qualifiers.noQualifiers())
        assertThat(Qualifiers.qualifiers(emptyList())).isSame(Qualifiers.noQualifiers())
        assertThat(Qualifiers.qualifiers(emptyList<Qualifier>() as Iterable<Qualifier>))
                .isSame(Qualifiers.noQualifiers())
    }

    @Nested
    @DisplayName("set")
    inner class SetTests {

        @Test
        fun `sets qualifiers`() {

            val qualifiers = Qualifiers.noQualifiers().set(testQualifier())

            assertThat(qualifiers).containsStrictly(testQualifier())
            assertThat(qualifiers.set(testQualifier2()))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.noQualifiers().set(testQualifier(), testQualifier2()))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `does not duplicate qualifiers`() {
            assertThat(Qualifiers.noQualifiers().set(testQualifier(), testQualifier1()))
                    .containsStrictly(testQualifier1())
        }

        @Test
        fun `updates qualifiers with the same signature`() {

            val initial = Qualifiers.qualifiers(testQualifier())
            val updated = initial.set(testQualifier1())

            assertThat(updated)
                    .notToBe(initial)
                    .containsNot(testQualifier())
                    .containsStrictly(testQualifier1())
        }

        @Test
        fun `returns the same instance when nothing added`() {

            val qualifiers = Qualifiers.qualifiers(testQualifier())

            assertThat(qualifiers.set()).isSame(qualifiers)
        }

        @Test
        fun `returns the same instance when similar qualifier re-added`() {

            val qualifiers = Qualifiers.qualifiers(testQualifier())

            assertThat(qualifiers.set(testQualifier())).isSame(qualifiers)
        }

    }

    @Nested
    @DisplayName("setAll")
    inner class SetAllTests {

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.noQualifiers().setAll(Qualifiers.qualifiers(testQualifier(), testQualifier2())))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `returns the same instance when nothing added`() {

            val qualifiers = Qualifiers.qualifiers(testQualifier())

            assertThat(qualifiers.fulfill(Qualifiers.noQualifiers())).isSame(qualifiers)
        }

        @Test
        fun `does not duplicate qualifiers`() {
            assertThat(Qualifiers.noQualifiers().setAll(Qualifiers.qualifiers(testQualifier(), testQualifier1()))).
                    containsStrictly(testQualifier1())
        }

    }

    @Nested
    @DisplayName("fulfill")
    inner class FulfillTests {

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.noQualifiers().fulfill(Qualifiers.qualifiers(testQualifier(), testQualifier2())))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `does not update qualifiers with the same signature`() {

            val initial = Qualifiers.qualifiers(testQualifier())
            val updated = initial.fulfill(Qualifiers.qualifiers(testQualifier1()))

            assertThat(updated).isSame(initial)
        }

    }

    @Nested
    @DisplayName("exclude")
    inner class ExcludeTests {

        @Test
        fun `excludes qualifiers with the same signature only`() {
            assertThat(
                    Qualifiers.qualifiers(testQualifier(), testQualifier2())
                            .exclude(Qualifiers.qualifiers(testQualifier1())))
                            .containsStrictly(testQualifier2())
        }

        @Test
        fun `excludes nothing from empty qualifiers`() {
            assertThat(Qualifiers.noQualifiers().exclude(Qualifiers.qualifiers(testQualifier1())))
                    .isSame(Qualifiers.noQualifiers())
        }

        @Test
        fun `returns the same instance after removing empty qualifiers`() {

            val qualifiers = Qualifiers.qualifiers(testQualifier(), testQualifier2())

            assertThat(qualifiers.exclude(Qualifiers.noQualifiers()))
                    .isSame(qualifiers)
        }

        @Test
        fun `returns the same instance after removing nothing`() {

            val qualifiers = Qualifiers.qualifiers(testQualifier())

            assertThat(qualifiers.exclude(Qualifiers.qualifiers(testQualifier2())))
                    .isSame(qualifiers)
        }

    }

}
