package ru.hse.spb.myutman.cli

import com.xenomachina.argparser.ArgParser
import org.antlr.v4.runtime.CharStreams
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*

private fun setInput(s: String) {
    System.setIn(ByteArrayInputStream(s.toByteArray()))
}

class SubstitutionTests {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = File("").absolutePath
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
        env["PWD"] = File("").absolutePath
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
        setInput("""mama anarhia
                |
                |papa
                |stakan portveina""".trimMargin())
        val cat = Cat()
        assertEquals("""mama anarhia
                |
                |papa
                |stakan portveina""".trimMargin(), cat.execute())
    }

    @Test
    fun testShouldWorkCatWithArgs() {
        val cat = Cat(arrayOf("./src/test/resources/test", "./src/test/resources/test"))
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
    fun testShouldWorkLsWithOneArg() {
        val ls = Ls(arrayOf("./src/test"), null, env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testShouldWorkLsWithPipe() {
        val ls = Ls(emptyArray(), Echo(arrayOf("./src/test")), env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testShouldWorkLsWithArgs() {
        val ls = Ls(arrayOf("./src/test", "./src/test/resources"), null, env)
        assertEquals(""" ./src/test:
                |dir: kotlin
                |dir: resources
                | ./src/test/resources:
                |file: test
                |""".trimMargin(), ls.execute())
    }


    @Test
    fun testCdPoint() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("./src/test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src/test", env["PWD"])
    }

    @Test
    fun testCdStrange() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("./src/test/../.."), env)
        assertEquals("", cd.execute())
        assertEquals(root, env["PWD"])
    }

    @Test
    fun testCdJustPoint() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("."), env)
        assertEquals("", cd.execute())
        assertEquals(root, env["PWD"])
    }

    @Test
    fun testCdBack() {
        val root = env["PWD"]
        Cd(arrayOf("./src/test"), env).execute()
        val cd = Cd(arrayOf(".."), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src", env["PWD"])
    }

    @Test
    fun testCdSlash() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("$root/src/test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src/test", env["PWD"])
    }

    @Test
    fun testCdWord() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("src/test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src/test", env["PWD"])
    }

    @Test
    fun testCdLs() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("src/test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src/test", env["PWD"])
        val ls = Ls(emptyArray(), null, env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testCdCat() {
        println(File(".").absolutePath)
        val root = env["PWD"]
        val cd = Cd(arrayOf("src/test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root/src/test", env["PWD"])
        val cat = Cat(arrayOf("resources/test"), null, env)
        assertEquals("""mama anarhia
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
        setInput("""mama anarhia
                |
                |papa
                |stakan portveina""".trimMargin())
        val wc = WC()
        assertEquals("\t4\t5\t35", wc.execute())
    }

    @Test
    fun testShouldWorkWCWithArgs() {
        val wc = WC(arrayOf("./src/test/resources/test", "./src/test/resources/test"))
        assertEquals("""|	4	5	35	./src/test/resources/test
            |	4	5	35	./src/test/resources/test
            |	8	10	70	total""".trimMargin(), wc.execute())
    }

    @Test
    fun testAssignation() {
        val assignation = Assignation("buzz", "foo", env)
        assignation.execute()
        assertEquals("foo", env["buzz"])
    }

    @Test
    fun testShouldMatchWithoutArgs() {
        setInput("""|lol
            |lollipop
            |Lol""".trimMargin())
        val grep = Grep(arrayOf("lol"))
        assertEquals("""|lol
            |lollipop""".trimMargin(), grep.execute())
    }

    @Test
    fun testShouldMatchWholeWord() {
        setInput("""|lol
            |lollipop
            |Lol""".trimMargin())
        val grep = Grep(arrayOf("lol", "-w"))
        assertEquals("lol", grep.execute())
    }

    @Test
    fun testShouldMatchIgnoringCase() {
        setInput("""|lol
            |lollipop
            |Lol""".trimMargin())
        val grep = Grep(arrayOf("lol", "-i"))
        assertEquals("""|lol
            |lollipop
            |Lol""".trimMargin(), grep.execute())
    }

    @Test
    fun testShouldWriteAdditionalLines() {
        setInput("""|lol
            |kek
            |lollipop
            |Lol""".trimMargin())
        val grep = Grep(arrayOf("lol", "-wiA", "1"))
        assertEquals("""|lol
            |kek
            |Lol""".trimMargin(), grep.execute())
    }

    @Test
    fun testShouldSupportRegex() {
        setInput("""|1. first
            |still first
            |
            |2. second
            |13.
            |thirteenth
            |
        """.trimMargin())
        val grep = Grep(arrayOf("[0-9]+\\."))
        assertEquals("""|1. first
            |2. second
            |13.""".trimMargin(), grep.execute())
    }
}

class ParserTest {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = "."
    }

    @Test
    fun testShouldBreakIntoLexemes() {
        val lexer = Subst(CharStreams.fromString("\$hello\'\$hello hell\$o\' hell\$o"))
        val actual = buildString {
            for (token in lexer.allTokens) {
                append(token.text, " ", token.startIndex, " ", token.stopIndex, " ", when(token.type) {
                    Subst.SUBST -> "SUBST"
                    else -> "QUOTE"
                }, System.lineSeparator())
            }
        }
        val expected = "\$hello 0 5 SUBST${System.lineSeparator()}" +
                "'\$hello hell\$o' 6 20 QUOTE${System.lineSeparator()}" +
                "\$o 26 27 SUBST${System.lineSeparator()}"
        assertEquals(expected, actual)
    }

    @Test
    fun testShouldParseCat() {
        val command = "cat ./src/test/resources/test".parseCommand(env)
        assertEquals("""mama anarhia
            |
            |papa
            |stakan portveina""".trimMargin(), command?.execute())
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
        val command = "wc ./src/test/resources/test".parseCommand(env)
        assertEquals("""|	4	5	35	./src/test/resources/test
                        |	4	5	35	total""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldParseAssignment() {
        "foo=bazz".parseCommand(env)?.execute()
        assertEquals("bazz", env["foo"])
    }

    @Test
    fun testShouldParseExternalCommand() {
        val command = "head ./src/test/resources/test".parseCommand(env)
        assertEquals("""mama anarhia
            |
            |papa
            |stakan portveina""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldParsePipe() {
        val command = "cat ./src/test/resources/test | wc".parseCommand(env)
        assertEquals("""|	4	5	35""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldParseCatPipeGrep() {
        val command = "cat ./src/test/resources/test | grep na -A 1".parseCommand(env)
        assertEquals("""|mama anarhia
            |
            |stakan portveina""".trimMargin(), command?.execute())
    }

    @Test
    fun testShouldSupportParsingRegex() {
        setInput("""|1. first
            |still first
            |
            |2. second
            |13.
            |thirteenth
            |
        """.trimMargin())
        val grep = "grep \"[0-9]+\\.\"".parseCommand(env)
        assertEquals("""|1. first
            |2. second
            |13.""".trimMargin(), grep?.execute())
    }

    @Test
    fun testShouldParseGrepPattern() {
        val args = arrayOf("lol", "-wA", "5", "-i")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertEquals("lol", pattern)
        }
    }

    @Test
    fun testShouldParseGrepWord() {
        val args = arrayOf("lol", "-wA", "5", "-i")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertTrue(word)
        }
    }

    @Test
    fun testShouldNotParseGrepWord() {
        val args = arrayOf("lol", "-A", "5", "-i")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertFalse(word)
        }
    }

    @Test
    fun testShouldParseGrepIgnore() {
        val args = arrayOf("lol", "-wA", "5", "-i")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertTrue(ignore)
        }
    }

    @Test
    fun testShouldNotParseGrepIgnore() {
        val args = arrayOf("lol", "-wA", "5")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertFalse(ignore)
        }
    }

    @Test
    fun testShouldParseGrepAdditionaly() {
        val args = arrayOf("lol", "-wA", "5", "-i")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertEquals(5, additionally)
        }
    }

    @Test
    fun testShouldParseGrepAdditionalyDefault() {
        val args = arrayOf("lol", "-wi")
        ArgParser(args).parseInto(::GrepArgs).run {
            assertEquals(0, additionally)
        }
    }
}

class ParserSubstitutionIntegration {

    val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = File("").absolutePath
    }

    @Test
    fun parserShouldSubstitute() {
        env["foo"] = "\"echo lol\""
        val command = "\$foo".parseCommand(env)
        assertEquals("lol", command?.execute())
    }
}