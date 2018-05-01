package org.predicode.predicator

/**
 * Resolution rule match pattern.
 */
data class RulePattern(val terms: List<SimpleTerm>) {

    constructor(vararg terms: SimpleTerm) : this(listOf(*terms))

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

    fun fact() = Rule(this, Predicate.True)

    fun rule(predicate: Predicate) = Rule(this, predicate)

}
