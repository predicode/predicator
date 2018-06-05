package org.predicode.predicator

import org.predicode.predicator.Rule.Match
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.*

/**
 * A selector of matching predicate resolution rules.
 *
 * Selects matching predicate resolution rules.
 *
 * @param pattern rule search pattern.
 * @param knowns known resolutions.
 *
 * @return a [Flux] of [rule matches][Match].
 */
typealias RuleSelector = (RulePattern, Knowns) -> Flux<Match>

/**
 * Creates a selector among the given predicate resolution rules.
 *
 * @param rules rules to select matching ones from.
 */
fun selectOneOf(vararg rules: Rule): RuleSelector = MatchingRuleSelector(rules)

private class MatchingRuleSelector(private val rules: Array<out Rule>) : RuleSelector {

    override fun invoke(pattern: RulePattern, knowns: Knowns) =
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
