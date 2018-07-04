package org.predicode.predicator.grammar

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import org.junit.jupiter.api.Test
import org.predicode.predicator.grammar.QuotedName.ATOM_NAME
import org.predicode.predicator.grammar.QuotedName.KEYWORD_NAME


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
                .toBe("-data")
        assertThat(print("3d", quoted = ATOM_NAME))
                .toBe("'3d")
        assertThat(print("-data", quoted = ATOM_NAME))
                .toBe("'-data")
    }

    @Test
    fun `quotes unconditionally`() {
        assertThat(print("atom", quoted = ATOM_NAME, quoting = QuotingStyle.OPEN_QUOTE))
                .toBe("\'atom")
        assertThat(print("atom", quoted = ATOM_NAME, quoting = QuotingStyle.ALWAYS_QUOTE))
                .toBe("\'atom\'")
    }

    @Test
    fun `closes quote`() {
        assertThat(print("name+", quoted = ATOM_NAME))
                .toBe("name+'")
        assertThat(print("name+"))
                .toBe("name+")
    }

    private fun print(
            name: String,
            quoted: QuotedName = KEYWORD_NAME,
            quoting: QuotingStyle = QuotingStyle.AUTO_QUOTE): String =
            printName(name, quoted = quoted, quoting = quoting)

}
