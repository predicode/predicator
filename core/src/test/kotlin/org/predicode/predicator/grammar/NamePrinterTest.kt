package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assert
import org.junit.jupiter.api.Test


internal class NamePrinterTest {

    @Test
    fun `prints name`() {
        assert(print("name"))
                .toBe("name")
    }

    @Test
    fun `removes leading whitespaces`() {
        assert(print(" \t name"))
                .toBe("name")
    }

    @Test
    fun `removes trailing whitespaces`() {
        assert(print("name \t "))
                .toBe("name")
    }

    @Test
    fun `removes extra whitespaces`() {
        assert(print("keyword \r name"))
                .toBe("keyword name")
    }

    @Test
    fun `escapes symbols`() {
        assert(print("keyword.name"))
                .toBe("keyword\\.name")
        assert(print("keyword:name"))
                .toBe("keyword\\:name")
        assert(print("keyword#name"))
                .toBe("keyword\\#name")
        assert(print("keyword\$name"))
                .toBe("keyword\\\$name")
        assert(print("keyword_name"))
                .toBe("keyword\\_name")
        assert(print("keyword`name"))
                .toBe("keyword\\`name")
        assert(print("keyword'name"))
                .toBe("keyword\\'name")
        assert(print("keyword\"name"))
                .toBe("keyword\\\"name")
    }

    @Test
    fun `encodes symbols`() {
        assert(print("keyword\u0008name"))
                .toBe("keyword\\8\\name")
    }

    @Test
    fun `glues symbols of different classes`() {
        assert(print("name 2"))
                .toBe("name2")
        assert(print("name - 3 x 3"))
                .toBe("name-3x3")
        assert(print("d & g"))
                .toBe("d&g")
        assert(print("prefix: suffix"))
                .toBe("prefix\\:suffix")
    }

    @Test
    fun `opens quote`() {
        assert(print("3d"))
                .toBe("`3d")
        assert(print("-data"))
                .toBe("`-data")
    }

    @Test
    fun `quotes unconditionally`() {
        assert(print("atom", quote = SINGLE_QUOTE, quoting = QuotingStyle.OPEN_QUOTE))
                .toBe("\'atom")
        assert(print("atom", quote = SINGLE_QUOTE, quoting = QuotingStyle.ALWAYS_QUOTE))
                .toBe("\'atom\'")
    }

    @Test
    fun `closes quote`() {
        assert(print("name+"))
                .toBe("name+`")
    }

    private fun print(
            name: String,
            quote: CodePoint = BACKTICK,
            quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE): String =
            printName(name, quote = quote, quoting = quoting)

}
