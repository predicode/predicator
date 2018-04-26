package org.predicode.predicator

/**
 * Resolution rule match pattern.
 */
data class RulePattern(val terms: List<SimpleTerm>) {

    /**
     * Attempts to match against another pattern.
     *
     * This method is called for the pattern stored in the [RuleSet].
     *
     * @param rule a pattern to match against.
     * @param knowns known resolutions.
     *
     * @return updated knowns if the pattern matches, or `null` otherwise.
     */
    fun match(rule: RulePattern, knowns: Knowns): Knowns? {
        if (rule.terms.size != terms.size) return null

        var result = knowns.update()
        var index = 0

        @Suppress("UseWithIndex")
        for (term in terms) {
            result = term.match(rule.terms[index], result) ?: return null
            ++index
        }

        return result;
    }

}
