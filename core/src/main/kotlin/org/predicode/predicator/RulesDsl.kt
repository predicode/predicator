package org.predicode.predicator

import org.predicode.predicator.annotations.PredicatorDslMarker
import org.predicode.predicator.predicates.Predicate
import org.predicode.predicator.predicates.PredicateDsl
import org.predicode.predicator.predicates.predicateCall
import org.predicode.predicator.terms.*
import reactor.core.publisher.Flux


fun rulePattern(define: PlainTermsDsl.() -> Unit): Rule.Pattern =
        Rule.pattern(plainTerms(define))

fun rulePrefixPattern(define: PlainTermsDsl.() -> Unit): Rule.Pattern =
        Rule.prefixPattern(plainTerms(define))


@PredicatorDslMarker
interface RulesDsl {

    fun rule(rule: Rule)

    @JvmDefault
    fun on(condition: Rule.Pattern): RulesDsl.DefinitionDsl = object : RulesDsl.DefinitionDsl {
        override fun predicate(predicate: Predicate) =
                condition.rule(predicate).also { this@RulesDsl.rule(it) }
    }

    @PredicatorDslMarker
    interface DefinitionDsl {

        fun predicate(predicate: Predicate): Rule

    }

}


fun RulesDsl.on(define: PlainTermsDsl.() -> Unit): RulesDsl.DefinitionDsl =
        on(rulePattern(define))

fun RulesDsl.onPrefix(define: PlainTermsDsl.() -> Unit): RulesDsl.DefinitionDsl =
        on(rulePrefixPattern(define))

fun RulesDsl.onDefinition(variable: Variable, define: PlainTermsDsl.() -> Unit): RulesDsl.DefinitionDsl = on {
    term(variable)
    term(Keyword.definition())
    define()
}

fun RulesDsl.onDefinition(name: String, definition: PlainTermsDsl.() -> Unit): RulesDsl.DefinitionDsl =
        onDefinition(Variable.named(name), definition)


fun RulesDsl.DefinitionDsl.call(define: PredicateDsl.CallDsl.() -> Unit) =
        predicate(predicateCall(define))

fun RulesDsl.DefinitionDsl.phrase(define: PhraseDsl.() -> Unit) =
        predicate(Phrase(define))

@Suppress("ObjectLiteralToLambda")
fun RulesDsl.DefinitionDsl.resolveBy(resolve: (Predicate.Resolver) -> Flux<Knowns>) =
        predicate(Predicate { resolver -> resolve(resolver) })

