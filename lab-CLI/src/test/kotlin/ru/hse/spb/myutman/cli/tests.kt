package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import java.io.ByteArrayInputStream
import java.io.File
import java.io.PrintWriter
import java.util.*

private fun setInput(s: String) {
    System.setIn(ByteArrayInputStream(s.toByteArray()))
}

class SubstitutionTests {

    val env = HashMap<String, String>()
    val filename = "./src/test/resources/test".replace('/', File.separatorChar);
    val stringInFile = """mama anarhia
        |
        |papa
        |stakan portveina""".trimMargin()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val writer = PrintWriter(File(filename))
        writer.print(stringInFile)
        writer.close()
    }

    @After
    fun after() {
        File(filename).delete()
    }

    @Test
    fun testShouldSubstitute() {
        env.put("spulae", "munlae")
        assertEquals(" 'b \$lol \$kek aba' munlae'munlae' niggawhyyoutrippingetyourmood rae",
            "\$aba 'b \$lol \$kek aba' \$spulae\'munlae\' niggawhyyoutrippingetyourmood rae".substitution(env))
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

    @Test
    fun testUnquoteShouldUnqouteSingleQuotes() {
        assertEquals("bazz", "'bazz'".unquote())
    }

    @Test
    fun testUnquoteShouldUnqouteDoubleQuotes() {
        assertEquals("bazz", "\"bazz\"".unquote())
    }

    @Test
    fun testUnquoteShouldNotUnqouteWithoutQuotes() {
        assertEquals("bazz", "bazz".unquote())
    }
}

class CommandTest {

    val env = HashMap<String, String>()
    val filename = "./src/test/resources/test".replace('/', File.separatorChar);
    val stringInFile = """mama anarhia
        |
        |papa
        |stakan portveina""".trimMargin()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val writer = PrintWriter(File(filename))
        writer.print(stringInFile)
        writer.close()
    }

    @After
    fun after() {
        File(filename).delete()
    }

    @Test
    fun testShouldWorkEcho() {
        val echo = Echo(arrayOf("kek", "lol", "arbidol"))
        assertEquals("kek lol arbidol", echo.execute())
    }

    @Test
    fun testShouldWorkCatWithPipe() {
        val cat = Cat(pipe = Echo(arrayOf("kek", "lol", "arbidol")))
        assertEquals("kek lol arbidol", cat.execute())
    }

    @Test
    fun testShouldWorkCatWithStdin() {
        setInput(stringInFile.trimMargin())
        val cat = Cat()
        assertEquals(stringInFile, cat.execute())
    }

    @Test
    fun testShouldWorkCatWithArgs() {
        val cat = Cat(arrayOf(filename, filename))
        assertEquals("""mama anarhia
                |
                |papa
                |stakan portveina
                |mama anarhia
                |
                |papa
                |stakan portveina""".trimMargin(), cat.execute())
    }

    @Test
    fun testShouldWorkWCWithPipe() {
        val wc = WC(pipe = Echo(arrayOf("kek", "lol", "arbidol")))
        assertEquals("\t1\t3\t15", wc.execute())
    }

    @Test
    fun testShouldWorkWCWithStdin() {
        setInput(stringInFile)
        val wc = WC()
        assertEquals("\t4\t5\t35", wc.execute())
    }

    @Test
    fun testShouldWorkWCWithArgs() {
        val wc = WC(arrayOf(filename, filename))
        assertEquals("""|	4	5	35	${filename}
            |	4	5	35	${filename}
            |	8	10	70	total""".trimMargin(), wc.execute())
    }

    @Test
    fun testAssignation() {
        val assignation = Assignation("buzz", "foo", env)
        assignation.execute()
        assertEquals("foo", env["buzz"])
    }
}

class ParserTest {

    val env = HashMap<String, String>()
    val filename = "./src/test/resources/test".replace('/', File.separatorChar);
    val stringInFile = """mama anarhia
        |
        |papa
        |stakan portveina""".trimMargin()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val writer = PrintWriter(File(filename))
        writer.print(stringInFile)
        writer.close()
    }

    @After
    fun after() {
        File(filename).delete()
    }

    @Test
    fun testShouldBreakIntoLexemes() {
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

    @Test
    fun testShouldParseCat() {
        val command = "cat $filename".parseCommand(env)
        assertEquals(stringInFile, command?.execute())
    }

    @Test
    fun testShouldParseCatStdin() {
        setInput("""mama
            |
            |papa""".trimMargin())
        val command = "cat".parseCommand(env)
        assertEquals("""mama
            |
            |papa""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldParseWC() {
        val command = "wc $filename".parseCommand(env)
        assertEquals("""|	4	5	35	$filename
                        |	4	5	35	total""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldParseAssignment() {
        "foo=bazz".parseCommand(env)?.execute()
        assertEquals("bazz", env["foo"])
    }

    @Test
    fun testShouldParseExternalCommand() {
        val command = "head $filename".parseCommand(env)
        assertEquals(stringInFile, command?.execute())
    }

    @Test
    fun testShouldParsePipe() {
        val command = "cat $filename | wc".parseCommand(env)
        assertEquals("""|	4	5	35""".trimMargin(), command?.execute())
    }
}

class ParserSubstitutionIntegration {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
    }

    @Test
    fun parserShouldSubstitute() {
        env["foo"] = "\"echo lol\""
        val command = "\$foo".parseCommand(env)
        assertEquals("lol", command?.execute())
    }
}