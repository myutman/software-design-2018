package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.CharStreams
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import substitution
import java.io.ByteArrayInputStream

class EnvironmentTests {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
    }

    @Test
    fun testSubstitution() {
        env.put("spunlae", "munlae")
        assertEquals(" 'b \$lol \$kek aba' munlae'munlae' niggawhyyoutrippingetyourmood rae",
            "\$aba 'b \$lol \$kek aba' \$spunlae\'munlae\' niggawhyyoutrippingetyourmood rae".substitution(env))
    }
}

class TokensTest {

    @Test
    fun testTokens() {
        val lexer = Subst(CharStreams.fromString("\$hello\'\$hello hell\$o\' hell\$o"))
        val actual = buildString {
            for (token in lexer.allTokens) {
                append(token.text, " ", token.startIndex, " ", token.stopIndex, "\n")
            }
        }
        val expected = "\$hello 0 5\n\$o 26 27\n"
        assertEquals(expected, actual)
    }
}

class CommandTest {

    @Test
    fun testEcho() {
        val echo = Echo(arrayOf("kek", "lol", "arbidol"))
        assertEquals("kek lol arbidol\n", echo.execute())
    }

    @Test
    fun testCat() {
        val cat = Cat(pipe = Echo(arrayOf("kek", "lol", "arbidol")))
        assertEquals("kek lol arbidol\n", cat.execute())
    }

    private fun setInput(s: String) {
        System.setIn(ByteArrayInputStream(s.toByteArray()))
    }

    @Test
    fun testWC() {
        setInput("mama anarhia\n" +
                "\n" +
                "npapa\n" +
                "stakan portveina\n")
        val wc = WC()
        assertEquals("\t5\t5\t37", wc.execute())
    }
}