package org.predicode.predicator.grammar

/**
 * Names quoting style.
 */
enum class QuotingStyle {

    /**
     * Automatically quote names when necessary.
     *
     * The opening or closing quote would is added if the name starts with or ends with not allowed symbol
     * respectively.
     *
     * This is used when printing [keyword][org.predicode.predicator.Keyword] name inside a phrase.
     */
    AUTO_QUOTE,

    /**
     * Always add opening quote.
     *
     * The closing quote would is added too if the name ends with not allowed symbol.
     *
     * This is used when printing [atom][org.predicode.predicator.Atom]
     * and [variable][org.predicode.predicator.Variable] name inside a phrase.
     */
    OPEN_QUOTE,

    /**
     * Always add both opening abd closing quotes.
     */
    ALWAYS_QUOTE;

    /**
     * Whether to always add opening quote.
     */
    @get:JvmName("openQuote")
    val openQuote: Boolean get() = this != AUTO_QUOTE

    /**
     * Whether to always add closing quote.
     */
    @get:JvmName("closeQuote")
    val closeQuote: Boolean get() = this == ALWAYS_QUOTE

}
