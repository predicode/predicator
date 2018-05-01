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
    val resolutions: Map<Name, Resolution>

    /**
     * Resolution rule variable mappings.
     *
     * These are [simple terms][SimpleTerm] passed to resolution rule. I.e. variable values local to the rule.
     * When [variable][VariableTerm] is used as local variable value, it is the one from [original query][resolutions].
     */
    val mappings: Map<Name, SimpleTerm>

    /**
     * Constructs knowns without any mappings and with the given query variables unresolved.
     *
     * @param variables query variable names.
     */
    constructor(vararg variables: Name) {
        this.mappings = emptyMap()
        this.resolutions = variables.associateBy({ it }, { Resolution.Unresolved })
    }

    private constructor(resolutions: Map<Name, Resolution>, mappings: Map<Name, SimpleTerm>) {
        this.resolutions = resolutions
        this.mappings = mappings
    }

    /**
     * Maps local resolution rule variable to the new value.
     *
     * If the value already set it can not be updated, unless the value is a query variable. In the latter case the
     * target variable is resolved to previous value.
     *
     * @param name variable name local to resolution rule.
     * @param value new variable value.
     *
     * @return updated resolutions, or `null` if they can not be updated thus making corresponding rule effectively
     * unmatched.
     */
    fun map(name: Name, value: SimpleTerm): Knowns? =
            mappings[name].let { prev ->
                return when (prev) {
                    null -> Knowns(resolutions, mappings + (name to value)) // New mapping
                    value -> this // Mapping didn't change
                    is VariableTerm -> resolve(prev.name, prev)
                    else -> null // Can not update mapping
                }
            }

    /**
     * Returns the given query variable resolution.
     *
     * @param name query variable name
     *
     * @throws NoSuchElementException if there is no such variable in original query.
     */
    fun resolution(name: Name) = resolutions.getValue(name)

    /**
     * Resolves original query variable.
     *
     * It is an error attempting to resolve non-existing variable.
     *
     * @param name original query variable name.
     */
    fun resolve(name: Name, value: ResolvedTerm): Knowns? =
            resolution(name).let { resolution ->
                return when (resolution) {
                    Resolution.Unresolved -> // Not resolved yet
                        Knowns(resolutions + (name to Resolution.Resolved(value)), mappings) // Resolve
                    is Resolution.Resolved ->
                        takeIf { value == resolution.value } // Resolution can not change
                    is Resolution.Alias ->
                        resolve(resolution.aliased, value) // Resolve aliased variable
                }
            }

    /**
     * Creates knowns to update while [matching the rules][]
     */
    fun update(): Knowns = Knowns(this.resolutions, emptyMap())

    private fun resolve(name: Name, value: SimpleTerm): Knowns? =
            resolution(name).let { resolution ->
                return when (resolution) {
                    Resolution.Unresolved -> // Not resolved yet
                        when (value) {
                            is ResolvedTerm -> // Resolve
                                Knowns(resolutions + (name to Resolution.Resolved(value)), mappings)
                            is VariableTerm -> // Create alias
                                resolution(value.name).let {
                                    // Ensure aliased query variable exists
                                    return Knowns(resolutions + (name to Resolution.Alias(value.name)), mappings)
                                }
                        }
                    is Resolution.Resolved ->
                        takeIf { value == resolution.value } // Resolution can not change
                    is Resolution.Alias ->
                        resolve(resolution.aliased, value) // Resolve aliased variable
                }
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
        object Unresolved : Resolution()

        /**
         * Query variable is resolved to the given [term][Resolved.value].
         *
         * @property value resolved term.
         */
        data class Resolved(val value: ResolvedTerm) : Resolution()

        /**
         * Query variable is an alias for [another][Alias.aliased] one.
         *
         * @property aliased aliased query variable name.
         */
        data class Alias(val aliased: Name) : Resolution()

    }

}
