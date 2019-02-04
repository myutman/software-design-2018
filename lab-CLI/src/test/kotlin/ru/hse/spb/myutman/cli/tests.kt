package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.CharStreams
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import java.io.ByteArrayInputStream

class SubstitutionTests {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
    }

    @Test
    fun testShouldSubstitute() {
        env.put("spunlae", "munlae")
        assertEquals(" 'b \$lol \$kek aba' munlae'munlae' niggawhyyoutrippingetyourmood rae",
            "\$aba 'b \$lol \$kek aba' \$spunlae\'munlae\' niggawhyyoutrippingetyourmood rae".substitution(env))
    }

    @Test
    fun testShouldNotSubstituteInSingleQuotes() {
        env.put("foo", "bazz")
        assertEquals("'\$foo'", "'\$foo'".substitution(env))
    }

    @Test
    fun testShouldSubstituteInDoubleQuotes() {
        env.put("foo", "bazz")
        assertEquals("\"bazz\"", "\"\$foo\"".substitution(env))
    }

    @Test
    fun testShouldSubstituteInInnerSingleQutesInDoubleQuotes() {
        env.put("foo", "bazz")
        assertEquals("\"'bazz'\"", "\"'\$foo'\"".substitution(env))
    }

    @Test
    fun testShouldUnquoteSingleQuotedValueInDoubleQuotes() {
        env.put("foo", "\'bazz\'")
        assertEquals("\"bazz\"", "\"\$foo\"".substitution(env))
    }

    @Test
    fun testShouldUnquoteDoubleQuotedValueInDoubleQuotes() {
        env.put("foo", "\"bazz\"")
        assertEquals("\"bazz\"", "\"\$foo\"".substitution(env))
    }

    @Test
    fun testShouldUnquoteSingleQuotedValueInInnerSingleQuotesInDoubleQuotes() {
        env.put("foo", "'bazz'")
        assertEquals("\"'bazz'\"", "\"'\$foo'\"".substitution(env))
    }

    @Test
    fun testShouldUnquoteDoubleQuotedValueInnerSingleQuotesInDoubleQuotes() {
        env.put("foo", "\"bazz\"")
        assertEquals("\"'bazz'\"", "\"'\$foo'\"".substitution(env))
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

class ParserTest {

    @Test
    fun testTokens() {
        val lexer = Subst(CharStreams.fromString("\$hello\'\$hello hell\$o\' hell\$o"))
        val actual = buildString {
            for (token in lexer.allTokens) {
                append(token.text, " ", token.startIndex, " ", token.stopIndex, " ", when(token.type) {
                    Subst.SUBST -> "SUBST"
                    else -> "QUOTE"
                }, "\n")
            }
        }
        val expected = "\$hello 0 5 SUBST\n" +
                "'\$hello hell\$o' 6 20 QUOTE\n" +
                "\$o 26 27 SUBST\n"
        assertEquals(expected, actual)
    }
}