package org.predicode.predicator

/**
 * Predicate resolver to resolve predicates against.
 *
 * An instance of this class is immutable.
 */
interface PredicateResolver {

    /**
     * Known variable mappings and resolutions.
     */
    val knowns: Knowns

    /**
     * Resolution rules selector.
     */
    val ruleSelector: Rule.Selector

    /**
     * Constructs new predicate resolver based on this one with the given variable mappings an resolutions.
     *
     * @param knowns new variable mappings an resolutions.
     */
    @JvmDefault
    fun withKnowns(knowns: Knowns): PredicateResolver = object : PredicateResolver {
        override val knowns = knowns
        override val ruleSelector = this@PredicateResolver.ruleSelector
    }

}
