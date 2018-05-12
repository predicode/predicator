package org.predicode.predicator

class UnknownVariable : RuntimeException {

    val variable: Variable

    constructor(_variable: Variable, _cause: Throwable?) : this(variable = _variable, cause = _cause)

    @JvmOverloads
    constructor(
            variable: Variable,
            message: String? = null,
            cause: Throwable? = null) : super(message ?: "Unknown variable $variable", cause) {
        this.variable = variable
    }

    companion object {

        @JvmStatic
        private val serialVersionUID: Long = 1

    }

}
