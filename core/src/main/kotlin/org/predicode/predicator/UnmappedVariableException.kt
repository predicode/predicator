package org.predicode.predicator

/**
 * Exception thrown when attempt is made to access the variable without [mapping][Knowns.mapping].
 *
 * @constructor Constructs new exception instance.
 *
 * @param variable unmapped variable.
 * @param message optional message.
 */
class UnmappedVariableException @JvmOverloads constructor(val variable: Variable, message: String? = null) :
        NoSuchElementException(message ?: "Unmapped variable $variable") {

    companion object {

        @JvmStatic
        private val serialVersionUID: Long = 1

    }

}
