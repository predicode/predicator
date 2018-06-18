package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.assertThat
import org.junit.jupiter.api.Test
import org.predicode.predicator.grammar.CodePoints.BACKTICK
import org.predicode.predicator.grammar.CodePoints.SINGLE_QUOTE


internal class NamePrinterTest {

    @Test
    fun `prints name`() {
        assertThat(print("name"))
                .toBe("name")
    }

    @Test
    fun `removes leading whitespaces`() {
        assertThat(print(" \t name"))
                .toBe("name")
    }

    @Test
    fun `removes trailing whitespaces`() {
        assertThat(print("name \t "))
                .toBe("name")
    }

    @Test
    fun `removes extra whitespaces`() {
        assertThat(print("keyword \r name"))
                .toBe("keyword name")
    }

    @Test
    fun `escapes symbols`() {
        assertThat(print("keyword.name"))
                .toBe("keyword\\.name")
        assertThat(print("keyword:name"))
                .toBe("keyword\\:name")
        assertThat(print("keyword#name"))
                .toBe("keyword\\#name")
        assertThat(print("keyword\$name"))
                .toBe("keyword\\\$name")
        assertThat(print("keyword_name"))
                .toBe("keyword\\_name")
        assertThat(print("keyword`name"))
                .toBe("keyword\\`name")
        assertThat(print("keyword'name"))
                .toBe("keyword\\'name")
        assertThat(print("keyword\"name"))
                .toBe("keyword\\\"name")
    }

    @Test
    fun `encodes symbols`() {
        assertThat(print("keyword\u0008name"))
                .toBe("keyword\\8\\name")
    }

    @Test
    fun `glues symbols of different classes`() {
        assertThat(print("name - 3 x 3"))
                .toBe("name-3 x 3")
        assertThat(print("d & g"))
                .toBe("d&g")
        assertThat(print("prefix: suffix"))
                .toBe("prefix\\:suffix")
    }

    @Test
    fun `does not glue numbers and letters`() {
        assertThat(print("name 1"))
                .toBe("name 1")
        assertThat(print("name2"))
                .toBe("name2")
        assertThat(print("name 3 d"))
                .toBe("name 3 d")
        assertThat(print("name 3d"))
                .toBe("name 3d")
    }

    @Test
    fun `opens quote`() {
        assertThat(print("3d"))
                .toBe("`3d")
        assertThat(print("-data"))
                .toBe("`-data")
    }

    @Test
    fun `quotes unconditionally`() {
        assertThat(print("atom", quote = SINGLE_QUOTE, quoting = QuotingStyle.OPEN_QUOTE))
                .toBe("\'atom")
        assertThat(print("atom", quote = SINGLE_QUOTE, quoting = QuotingStyle.ALWAYS_QUOTE))
                .toBe("\'atom\'")
    }

    @Test
    fun `closes quote`() {
        assertThat(print("name+"))
                .toBe("name+`")
    }

    private fun print(
            name: String,
            quote: CodePoint = BACKTICK,
            quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE): String =
            printName(name, quote = quote, quoting = quoting)

}
