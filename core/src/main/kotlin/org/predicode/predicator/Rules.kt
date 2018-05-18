@file:JvmName("Rules")
package org.predicode.predicator

import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.*

/**
 * Creates scanning predicate resolution rule selector.
 *
 * It scans the given [rules] for the matches.
 *
 * @param rules rules to scan.
 */
fun ruleMatcher(vararg rules: Rule): Rule.Selector = SequentialRuleSelector(rules)

private class SequentialRuleSelector(private val rules: Array<out Rule>) : Rule.Selector {

    override fun ruleMatches(pattern: RulePattern, knowns: Knowns) =
            rules.toFlux().flatMap { rule ->
                Mono.justOrEmpty(rule.match(pattern, knowns))
            }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SequentialRuleSelector

        return Arrays.equals(rules, other.rules)
    }

    override fun hashCode() = Arrays.hashCode(rules)

    override fun toString(): String {
        return "{\n  ${rules.joinToString("\n  ")}\n}"
    }

}
