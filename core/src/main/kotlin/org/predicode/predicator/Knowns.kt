package org.predicode.predicator

/**
 * Known mappings and resolutions.
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
     * These are [simple terms][SimpleTerm] passed to resolution rule. I.e. variable values local to the rule.
     * When [variable][Variable] is used as local variable value, it is the one from [original query][resolutions].
     */
    private val mappings: Map<Variable, SimpleTerm>

    /**
     * Constructs knowns without any mappings and with the given query variables unresolved.
     *
     * @param variables query variables.
     */
    constructor(vararg variables: Variable) {
        this.mappings = emptyMap()
        this.resolutions = variables.associateBy({ it }, { Resolution.Unresolved })
    }

    private constructor(resolutions: Map<Variable, Resolution>, mappings: Map<Variable, SimpleTerm>) {
        this.resolutions = resolutions
        this.mappings = mappings
    }

    /**
     * Returns the given local resolution rule variable resolution.
     *
     * @param variable a variable, local to resolution rule.
     *
     * @throws UnknownVariable if variable is not mapped.
     */
    fun mapping(variable: Variable): SimpleTerm =
            mappings.getOrElse(variable) { throw UnknownVariable(variable, "Unmapped variable $variable") }

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
    fun map(variable: Variable, value: SimpleTerm): Knowns? =
            mappings[variable].let { prev ->
                return when (prev) {
                    null -> Knowns(resolutions, mappings + (variable to value)) // New mapping
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
     * @throws NoSuchElementException if there is no such variable in original query.
     */
    fun resolution(variable: Variable) = resolutions.getValue(variable)

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
                        Knowns(resolutions + (variable to Resolution.Resolved(value)), mappings) // Resolve
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
    fun update(): Knowns = takeIf { mappings.isEmpty() } ?: Knowns(resolutions, emptyMap())

    private fun resolve(variable: Variable, value: SimpleTerm): Knowns? =
            resolution(variable).let { resolution ->
                return when (resolution) {
                    Resolution.Unresolved -> // Not resolved yet
                        when (value) {
                            is ResolvedTerm -> // Resolve
                                Knowns(resolutions + (variable to Resolution.Resolved(value)), mappings)
                            is Keyword -> // Keywords are never substituted as variable names
                                null
                            is Variable -> // Create alias
                                resolution(value).let {
                                    // Ensure aliased query variable exists
                                    Knowns(resolutions + (variable to Resolution.Alias(value)), mappings)
                                }
                        }
                    is Resolution.Resolved ->
                        takeIf { value == resolution.value } // Resolution can not change
                    is Resolution.Alias ->
                        resolve(resolution.aliased, value) // Resolve aliased variable
                }
            }

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
        return "Knowns(resolutions=$resolutions, mappings=$mappings)"
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

}
