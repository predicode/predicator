package org.predicode.predicator.terms

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.predicode.predicator.Knowns
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.TestPredicateResolver
import org.predicode.predicator.testutils.isEmpty
import org.predicode.predicator.testutils.toContain


class KeywordTest {

    lateinit var knowns: Knowns
    lateinit var resolver: Predicate.Resolver

    @BeforeEach
    fun `create knowns`() {
        knowns = Knowns.none()
        resolver = TestPredicateResolver(knowns)
    }

    @Test
    fun `string representation`() {
        assertThat(Keyword.named("keyword").toString())
                .toBe("`keyword`")
    }

    @Test
    fun `matches keyword with the same name`() {
        assertThat(Keyword.named("name1").match(Keyword.named("name1"), knowns))
                .toContain(knowns)
    }

    @Test
    fun `does not match keyword with another name`() {
        assertThat(Keyword.named("name1").match(Keyword.named("name2"), knowns)).isEmpty()
    }

    @Test
    fun `does not match placeholder`() {
        assertThat(Keyword.named("name").match(Placeholder.placeholder(), knowns)).isEmpty()
    }

    @Test
    fun `does not match other terms`() {

        val keyword = Keyword.named("name")

        assertThat(keyword.match(Atom.named("name"), knowns)).isEmpty()
        assertThat(keyword.match(Value.raw(123), knowns)).isEmpty()
        assertThat(keyword.match(Variable.named("name"), knowns)).isEmpty()
    }

    @Test
    fun `expands to itself`() {

        val keyword = Keyword.named("name")

        assertThat(keyword.expand(resolver))
                .toContain(Term.Expansion(keyword, knowns))
    }

}
