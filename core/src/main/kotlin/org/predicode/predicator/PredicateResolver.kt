package org.predicode.predicator

interface PredicateResolver {

    val knowns: Knowns

    val ruleSelector: Rule.Selector

    @JvmDefault
    fun withKnowns(_knowns: Knowns): PredicateResolver = object : PredicateResolver {
        override val knowns = _knowns
        override val ruleSelector = this@PredicateResolver.ruleSelector
    }

}
