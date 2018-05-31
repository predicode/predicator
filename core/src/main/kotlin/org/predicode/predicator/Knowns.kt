package org.predicode.predicator

import org.predicode.predicator.Knowns.Resolution.*
import java.util.function.BiFunction

/**
 * Known local variable mappings and query variable resolutions.
 *
 * Instances of this class are immutable. Every modification method returns a new instance based on current one.
 */
class Knowns {

    /**
     * Original query variable resolutions.
     *
     * These are [resolved terms][ResolvedTerm] when resolved. Once resolved they can not change any more.
     *
     * Query variable names are set only once via constructor. The list of query variables can not change after that.
     */
    private val resolutions: Map<Variable, Resolution>

    /**
     * Resolution rule variable mappings.
     *
     * These are [plain terms][PlainTerm] passed to resolution rule. I.e. variable values local to the rule.
     * When [variable][Variable] is used as local variable value, it is the one from [original query][resolutions].
     */
    private val mappings: Map<Variable, PlainTerm>

    private val rev: Int

    /**
     * Constructs knowns without any mappings and with the given query variables unresolved.
     *
     * @param variables query variables.
     */
    constructor(vararg variables: Variable) {
        this.mappings = emptyMap()
        this.resolutions = variables.associateBy({ it }, { Resolution.Unresolved })
        this.rev = 0
    }

    private constructor(
            proto: Knowns,
            resolutions: Map<Variable, Resolution> = proto.resolutions,
            mappings: Map<Variable, PlainTerm> = proto.mappings,
            rev: Int = proto.rev) {
        this.resolutions = resolutions
        this.mappings = mappings
        this.rev = rev
    }

    /**
     * Handles the given local variable mapping.
     *
     * If the given variable is not mapped yet, then declares a local variable and maps the given variable to it.
     * The updated knowns are passed to [handler].
     *
     * @param variable a variable, local to resolution rule.
     * @param handler a handler function accepting mapping and updated knowns as argument and returning arbitrary value.
     *
     * @return the value returned by [handler], or `null` if mapping is impossible.
     */
    fun <R : Any> mapping(variable: Variable, handler: BiFunction<in PlainTerm, in Knowns, out R>): R? =
            mappings[variable]
                    ?.let { mapping -> handler.apply(mapping, this) }
                    ?: declareLocal(variable, handler)

    /**
     * Declares the given variable as local, if not declared yet.
     *
     * @param variable a variable to declare as local one.
     * @param handler a handler function accepting declared local and updated knowns as argument and returning arbitrary
     * value.
     *
     * @return the value returned by [handler].
     */
    fun <R> declareLocal(variable: Variable, handler: BiFunction<in Variable, in Knowns, out R>): R =
            LocalVariable(variable, rev).let { local ->
                declareLocal(local).let {
                    knowns -> handler.apply(
                        local,
                        Knowns(knowns, mappings = knowns.mappings + (variable to local)))
                }
            }

    /**
     * Maps local resolution rule variable to the new value.
     *
     * If the value already set it can not be updated, unless the value is a query variable. In the latter case the
     * target variable is resolved to previous value.
     *
     * @param variable variable local to resolution rule.
     * @param value new variable value.
     *
     * @return updated resolutions, or `null` if they can not be updated thus making corresponding rule effectively
     * unmatched.
     */
    fun map(variable: Variable, value: MappedTerm): Knowns? =
            mappings[variable].let { prev ->
                return when (prev) {
                    null -> Knowns(
                            this,
                            mappings = mappings + (variable to value.also {
                                if (it is Variable) resolution(it) // Ensure query variable exists
                            })) // New mapping
                    value -> this // Mapping didn't change
                    is Variable -> resolve(prev, value)
                    else -> null // Can not update mapping
                }
            }

    /**
     * Returns the given query variable resolution.
     *
     * @param variable query variable.
     *
     * @throws UnknownVariableException if there is no such variable in original query.
     */
    fun resolution(variable: Variable): Resolution =
            resolutions.getOrElse(variable) { throw UnknownVariableException(variable) }

    /**
     * Resolves original query variable.
     *
     * It is an error attempting to resolve non-existing variable.
     *
     * @param variable original query variable.
     */
    fun resolve(variable: Variable, value: ResolvedTerm): Knowns? =
            resolution(variable).let { resolution ->
                return when (resolution) {
                    Resolution.Unresolved -> // Not resolved yet
                        Knowns(this, resolutions = resolutions + (variable to Resolution.Resolved(value))) // Resolve
                    is Resolution.Resolved ->
                        takeIf { value == resolution.value } // Resolution can not change
                    is Resolution.Alias ->
                        resolve(resolution.aliased, value) // Resolve aliased variable
                }
            }

    /**
     * Creates knowns to update while [matching the rules][].
     *
     * Such knowns contain no mappings.
     */
    fun startMatching(): Knowns =
            Knowns(this, mappings = emptyMap(), rev = rev + 1)

    private fun resolve(variable: Variable, value: MappedTerm): Knowns? =
            resolution(variable).let { resolution ->
                return when (resolution) {
                    Resolution.Unresolved -> // Not resolved yet
                        when (value) {
                            is ResolvedTerm -> // Resolve
                                Knowns(this, resolutions = resolutions + (variable to Resolution.Resolved(value)))
                            is Variable -> // Create an alias
                                resolution(value).let {
                                    // Ensure aliased query variable exists
                                    Knowns(this, resolutions = resolutions + (variable to Resolution.Alias(value)))
                                }
                        }
                    is Resolution.Resolved ->
                        takeIf { value == resolution.value } // Resolution can not change
                    is Resolution.Alias ->
                        resolve(resolution.aliased, value) // Resolve aliased variable
                }
            }

    private fun declareLocal(local: LocalVariable): Knowns =
            resolutions[local]
                    ?.let { this }
                    ?: Knowns(
                            this,
                            mappings = mappings + (local.variable to local),
                            resolutions = resolutions + (local to Resolution.Unresolved))


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Knowns

        if (resolutions != other.resolutions) return false
        if (mappings != other.mappings) return false

        return true
    }

    override fun hashCode(): Int {

        var result = resolutions.hashCode()

        result = 31 * result + mappings.hashCode()

        return result
    }

    override fun toString(): String {
        return "Knowns(resolutions=$resolutions, mappings=$mappings, rev. $rev)"
    }

    /**
     * Query variable resolution.
     *
     * Can be one of:
     * - [unresolved][Unresolved],
     * - [resolved][Resolved], or
     * - [alias][Alias] for another variable.
     */
    sealed class Resolution {

        /**
         * Query variable is not resolved.
         */
        object Unresolved : Resolution() {

            override fun toString() = "Unresolved"

        }

        /**
         * Query variable is resolved to the given [term][Resolved.value].
         *
         * @property value resolved term.
         */
        data class Resolved(val value: ResolvedTerm) : Resolution() {

            override fun toString() = "Resolved(value=$value)"

        }

        /**
         * Query variable is an alias for [another][Alias.aliased] one.
         *
         * @property aliased aliased query variable.
         */
        data class Alias(val aliased: Variable) : Resolution() {

            override fun toString() = "Alias(aliased=$aliased)"

        }

    }

    private data class LocalVariable(val variable: Variable, val rev: Int) : Variable() {

        override val name = variable.name

        override fun toString(): String = "$name #iteration"

    }

}

/**
 * Handles the given local variable mapping.
 *
 * If the given variable is not mapped yet, then declares a local variable and maps the given variable to it.
 * The updated knowns are passed to [handler].
 *
 * @param variable a variable, local to resolution rule.
 * @param handler a handler function accepting mapping and updated knowns as argument and returning arbitrary value.
 *
 * @see [Knowns.mapping]
 */
fun <R : Any> Knowns.mapping(variable: Variable, handler: (PlainTerm, Knowns) -> R): R? =
        mapping(
                variable,
                BiFunction { mapping, knowns -> handler(mapping, knowns) })

/**
 * Handles the given local variable mapping.
 *
 * If the given variable is not mapped yet, then declares a local variable and maps the given variable to it.
 * The updated knowns are passed to [handler].
 *
 * @param variable a variable, local to resolution rule.
 * @param handler a handler function accepting mapping and updated knowns as argument and returning arbitrary value.
 *
 * @see [Knowns.mapping]
 */
fun <R> Knowns.declareLocal(variable: Variable, handler: (Variable, Knowns) -> R): R =
        declareLocal(
                variable,
                BiFunction { mapping, knowns -> handler(mapping, knowns) })
