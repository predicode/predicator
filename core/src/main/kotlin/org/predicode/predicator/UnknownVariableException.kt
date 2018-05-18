package org.predicode.predicator

/**
 * Exception thrown when attempt is made to access the variable not present in original query.
 * I.e. the variable without any [resolution][Knowns.resolution].
 *
 * This typically happens if the variable is not listed when initial [Knowns] constructed.
 *
 * @constructor Constructs new exception instance.
 *
 * @param variable unmapped variable.
 * @param message optional message.
 */
class UnknownVariableException @JvmOverloads constructor(val variable: Variable, message: String? = null) :
        NoSuchElementException(message ?: "Unknown variable $variable") {

    companion object {

        @JvmStatic
        private val serialVersionUID: Long = 1

    }

}
