package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import java.io.ByteArrayInputStream
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import java.util.*

private fun setInput(s: String) {
    System.setIn(ByteArrayInputStream(s.toByteArray()))
}

val filename = "./src/test/resources/test".replace('/', File.separatorChar);
val stringInFile = "mama anarhia" + System.lineSeparator() +
        "" + System.lineSeparator() +
        "papa" + System.lineSeparator() +
        "stakan portveina"
val wcAns = "\t4\t5\t${stringInFile.length}"
val wcTotal = wcAns + "\t" + filename + System.lineSeparator() +
        wcAns + "\t" + filename + System.lineSeparator() +
        "\t8\t10\t${stringInFile.length * 2}\ttotal"

class SubstitutionTests {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val file = File(filename)
        File(file.parent).mkdirs()
        val writer = PrintWriter(file)
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

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val file = File(filename)
        File(file.parent).mkdirs()
        val writer = PrintWriter(file)
        writer.print(stringInFile)
        writer.close()
    }

    @After
    fun after() {
        //File(filename).delete()
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
        setInput(stringInFile)
        val cat = Cat()
        assertEquals(stringInFile.trim(), cat.execute().trim())
    }

    @Test
    fun testShouldWorkCatWithArgs() {
        val cat = Cat(arrayOf(filename, filename))
        assertEquals(stringInFile + System.lineSeparator() + stringInFile, cat.execute())
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
        assertEquals(wcAns, wc.execute())
    }

    @Test
    fun testShouldWorkWCWithArgs() {
        val wc = WC(arrayOf(filename, filename))
        assertEquals(wcTotal, wc.execute())
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

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
        val file = File(filename)
        File(file.parent).mkdirs()
        val writer = PrintWriter(file)
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
        val str = "mama" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "papa"
        setInput(str)
        val command = "cat".parseCommand(env)
        assertEquals(str, command?.execute())
    }

    @Test
    fun testShouldParseWC() {
        val command = "wc $filename".parseCommand(env)
        assertEquals(wcAns + "\t" + filename + System.lineSeparator() +
                    wcAns + "\ttotal", command?.execute())
    }

    @Test
    fun testShouldParseAssignment() {
        "foo=bazz".parseCommand(env)?.execute()
        assertEquals("bazz", env["foo"])
    }

    @Test
    fun testShouldParseExternalLinuxCommand() {
        try {
            val command = "head $filename".parseCommand(env)
            assertEquals(stringInFile, command?.execute())
        } catch (ignore: CLIException) {
        }
    }

    @Test
    fun testShouldParsePipe() {
        val command = "cat $filename | wc".parseCommand(env)
        assertEquals(wcAns, command?.execute())
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
        env["foo"] = "\"echo\""
        val command = "\$foo lol".parseCommand(env)
        assertEquals("lol", command?.execute())
    }
}