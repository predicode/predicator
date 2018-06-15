package org.predicode.predicator

import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.*

@Suppress("NOTHING_TO_INLINE")
inline operator fun Rule.Selector.invoke(pattern: RulePattern, knowns: Knowns) = matchingRules(pattern, knowns)

@Suppress("NOTHING_TO_INLINE")
inline operator fun Rule.Match.component1(): Rule = rule

@Suppress("NOTHING_TO_INLINE")
inline operator fun Rule.Match.component2(): Knowns = knowns

/**
 * Creates a selector among the given predicate resolution rules.
 *
 * @param rules rules to select matching ones from.
 */
fun selectOneOf(vararg rules: Rule): Rule.Selector = MatchingRuleSelector(rules)

private class MatchingRuleSelector(private val rules: Array<out Rule>) : Rule.Selector {

    override fun matchingRules(pattern: RulePattern, knowns: Knowns) =
            rules.toFlux().flatMap { rule ->
                Mono.justOrEmpty(rule.match(pattern, knowns))
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchingRuleSelector

        return Arrays.equals(rules, other.rules)
    }

    override fun hashCode() = Arrays.hashCode(rules)

    override fun toString(): String {
        return "{\n  ${rules.joinToString("\n  ")}\n}"
    }

}
