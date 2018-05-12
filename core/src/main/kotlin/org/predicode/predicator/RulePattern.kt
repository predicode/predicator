package org.predicode.predicator

import java.util.*

/**
 * Resolution rule match pattern.
 */
class RulePattern(vararg _terms: SimpleTerm) {

    private val terms: Array<out SimpleTerm> = _terms

    /**
     * Attempts to match against another pattern.
     *
     * This method is called for the [rule condition][Rule.condition] with query pattern as argument.
     *
     * @param pattern a pattern to match against.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the pattern matches, or `null` otherwise.
     */
    fun match(pattern: RulePattern, knowns: Knowns): Knowns? {
        if (pattern.terms.size != terms.size) return null

        var result = knowns.update()
        var index = 0

        @Suppress("UseWithIndex")
        for (term in terms) {
            result = term.match(pattern.terms[index], result) ?: return null
            ++index
        }

        return result
    }

    /**
     * Creates a fact with this pattern as its [condition][Rule.condition].
     */
    fun fact() = Rule(this, TRUE)

    /**
     * Creates a resolution rule with this pattern as its [condition][Rule.condition].
     *
     * @param predicate predicate the constructed rule resolves to if this pattern matches.
     */
    fun rule(predicate: Predicate) = Rule(this, predicate)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RulePattern

        return Arrays.equals(terms, other.terms)

    }

    override fun hashCode(): Int {
        return Arrays.hashCode(terms)
    }

}
