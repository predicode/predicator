package org.predicode.predicator.predicates

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QualifiersTest {

    @Test
    fun `construction does not depend on qualifiers order`() {
        assertThat(Qualifiers.of(testQualifier(), testQualifier2()))
                .toBe(Qualifiers.of(testQualifier2(), testQualifier()))
    }

    @Test
    fun `contains qualifiers`() {

        val qualifiers = Qualifiers.of(testQualifier(), testQualifier2())

        assertThat(qualifiers)
                .contains(testQualifier(), testQualifier2())
                .hasSize(2)
    }

    @Test
    fun `does not duplicate qualifiers`() {

        val qualifiers = Qualifiers.of(testQualifier(), testQualifier())

        assertThat(qualifiers).containsStrictly(testQualifier())
    }

    @Test
    fun `build singleton on empty input`() {
        assertThat(Qualifiers.of()).isSameAs(Qualifiers.none())
        assertThat(Qualifiers.of(emptyList())).isSameAs(Qualifiers.none())
        assertThat(Qualifiers.of(emptyList<Qualifier>() as Iterable<Qualifier>))
                .isSameAs(Qualifiers.none())
    }

    @Nested
    @DisplayName("set")
    inner class SetTests {

        @Test
        fun `sets qualifiers`() {

            val qualifiers = Qualifiers.none().set(testQualifier())

            assertThat(qualifiers).containsStrictly(testQualifier())
            assertThat(qualifiers.set(testQualifier2()))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.none().set(testQualifier(), testQualifier2()))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `does not duplicate qualifiers`() {
            assertThat(Qualifiers.none().set(testQualifier(), testQualifier1()))
                    .containsStrictly(testQualifier1())
        }

        @Test
        fun `updates qualifiers with the same signature`() {

            val initial = Qualifiers.of(testQualifier())
            val updated = initial.set(testQualifier1())

            assertThat(updated)
                    .notToBe(initial)
                    .containsNot(testQualifier())
                    .containsStrictly(testQualifier1())
        }

        @Test
        fun `returns the same instance when nothing added`() {

            val qualifiers = Qualifiers.of(testQualifier())

            assertThat(qualifiers.set()).isSameAs(qualifiers)
        }

        @Test
        fun `returns the same instance when similar qualifier re-added`() {

            val qualifiers = Qualifiers.of(testQualifier())

            assertThat(qualifiers.set(testQualifier())).isSameAs(qualifiers)
        }

    }

    @Nested
    @DisplayName("setAll")
    inner class SetAllTests {

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.none().setAll(Qualifiers.of(testQualifier(), testQualifier2())))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `returns the same instance when nothing added`() {

            val qualifiers = Qualifiers.of(testQualifier())

            assertThat(qualifiers.fulfill(Qualifiers.none())).isSameAs(qualifiers)
        }

        @Test
        fun `does not duplicate qualifiers`() {
            assertThat(Qualifiers.none().setAll(Qualifiers.of(testQualifier(), testQualifier1()))).
                    containsStrictly(testQualifier1())
        }

    }

    @Nested
    @DisplayName("fulfill")
    inner class FulfillTests {

        @Test
        fun `sets all qualifiers`() {
            assertThat(Qualifiers.none().fulfill(Qualifiers.of(testQualifier(), testQualifier2())))
                    .contains(testQualifier(), testQualifier2())
                    .hasSize(2)
        }

        @Test
        fun `does not update qualifiers with the same signature`() {

            val initial = Qualifiers.of(testQualifier())
            val updated = initial.fulfill(Qualifiers.of(testQualifier1()))

            assertThat(updated).isSameAs(initial)
        }

    }

    @Nested
    @DisplayName("exclude")
    inner class ExcludeTests {

        @Test
        fun `excludes qualifiers with the same signature only`() {
            assertThat(
                    Qualifiers.of(testQualifier(), testQualifier2())
                            .exclude(Qualifiers.of(testQualifier1())))
                            .containsStrictly(testQualifier2())
        }

        @Test
        fun `excludes nothing from empty qualifiers`() {
            assertThat(Qualifiers.none().exclude(Qualifiers.of(testQualifier1())))
                    .isSameAs(Qualifiers.none())
        }

        @Test
        fun `returns the same instance after removing empty qualifiers`() {

            val qualifiers = Qualifiers.of(testQualifier(), testQualifier2())

            assertThat(qualifiers.exclude(Qualifiers.none()))
                    .isSameAs(qualifiers)
        }

        @Test
        fun `returns the same instance after removing nothing`() {

            val qualifiers = Qualifiers.of(testQualifier())

            assertThat(qualifiers.exclude(Qualifiers.of(testQualifier2())))
                    .isSameAs(qualifiers)
        }

    }

}
