package org.predicode.predicator

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.PredicateDsl
import org.predicode.predicator.predicates.newPredicateCall
import org.predicode.predicator.terms.*
import reactor.core.publisher.Flux


fun newRulePattern(define: PlainTermsDsl.() -> Unit): Rule.Pattern =
        Rule.pattern(newPlainTerms(define))

fun newRulePrefixPattern(define: PlainTermsDsl.() -> Unit): Rule.Pattern =
        Rule.prefixPattern(newPlainTerms(define))


@PredicatorDslMarker
interface RulesDsl {

    fun rule(rule: Rule)

    @JvmDefault
    fun on(condition: Rule.Pattern): Definition = object : Definition {
        override fun predicate(predicate: Predicate) =
                condition.rule(predicate).also { this@RulesDsl.rule(it) }
    }

    @PredicatorDslMarker
    interface Definition {

        fun predicate(predicate: Predicate): Rule

    }

}


fun RulesDsl.on(define: PlainTermsDsl.() -> Unit): RulesDsl.Definition =
        on(newRulePattern(define))

fun RulesDsl.onPrefix(define: PlainTermsDsl.() -> Unit): RulesDsl.Definition =
        on(newRulePrefixPattern(define))

fun RulesDsl.onDefinition(variable: Variable, define: PlainTermsDsl.() -> Unit): RulesDsl.Definition = on {
    term(variable)
    term(Keyword.definition())
    define()
}

fun RulesDsl.onDefinition(name: String, definition: PlainTermsDsl.() -> Unit): RulesDsl.Definition =
        onDefinition(Variable.named(name), definition)


fun RulesDsl.Definition.call(define: PredicateDsl.Call.() -> Unit) =
        predicate(newPredicateCall(define))

fun RulesDsl.Definition.phrase(define: PhraseDsl.() -> Unit) =
        predicate(newPhrase(define))

@Suppress("ObjectLiteralToLambda")
fun RulesDsl.Definition.resolveBy(resolve: (Predicate.Resolver) -> Flux<Knowns>) =
        predicate(Predicate { resolver -> resolve(resolver) })

