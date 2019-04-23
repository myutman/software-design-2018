package ru.hse.spb.myutman.cli

import com.xenomachina.argparser.ArgParser
import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.hse.spb.myutman.parser.Subst
import java.io.ByteArrayInputStream
import java.io.File
import java.io.PrintWriter
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

    private val env = HashMap<String, String>()

    @Before
    fun before() {
        env.clear()
        env["PWD"] = File("").absolutePath
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

    private val env = HashMap<String, String>()
    private val testDir = "${File.separator}src${File.separator}test"
    private val testFile = "src${File.separator}test${File.separator}resources${File.separator}test"

    @Before
    fun before() {
        env.clear()
        env["PWD"] = File("").absolutePath
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
    fun testShouldWorkLsWithOneArg() {
        val ls = Ls(arrayOf(".$testDir"), null, env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testShouldWorkLsWithPipe() {
        val ls = Ls(emptyArray(), Echo(arrayOf(".$testDir")), env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testShouldWorkLsWithArgs() {
        val ls = Ls(arrayOf(".$testDir", ".$testDir${File.separator}resources"), null, env)
        assertEquals(""" .${File.separator}src${File.separator}test:
                |dir: kotlin
                |dir: resources
                | .${File.separator}src${File.separator}test${File.separator}resources:
                |file: test
                |""".trimMargin(), ls.execute())
    }


    @Test
    fun testCdPoint() {
        val root = env["PWD"]
        val cd = Cd(arrayOf(".$testDir"), env)
        assertEquals("", cd.execute())
        assertEquals("$root$testDir", env["PWD"])
    }

    @Test
    fun testCdStrange() {
        val root = env["PWD"]
        val cd = Cd(arrayOf(".$testDir${File.separator}..${File.separator}.."), env)
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
        Cd(arrayOf(".$testDir"), env).execute()
        val cd = Cd(arrayOf(".."), env)
        assertEquals("", cd.execute())
        assertEquals("$root${File.separator}src", env["PWD"])
    }

    @Test
    fun testCdSlash() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("$root$testDir"), env)
        assertEquals("", cd.execute())
        assertEquals("$root$testDir", env["PWD"])
    }

    @Test
    fun testCdEmpty() {
        val cd = Cd(emptyArray(), env)
        assertEquals("", cd.execute())
        assertEquals(System.getProperty("user.home"), env["PWD"])
    }

    @Test
    fun testCdWord() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("src${File.separator}test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root$testDir", env["PWD"])
    }

    @Test
    fun testCdLs() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("src${File.separator}test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root$testDir", env["PWD"])
        val ls = Ls(emptyArray(), null, env)
        assertEquals("""dir: kotlin
                |dir: resources
                |""".trimMargin(), ls.execute())
    }

    @Test
    fun testCdCat() {
        val root = env["PWD"]
        val cd = Cd(arrayOf("src${File.separator}test"), env)
        assertEquals("", cd.execute())
        assertEquals("$root$testDir", env["PWD"])
        val cat = Cat(arrayOf("resources${File.separator}test"), null, env)
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

    @Test
    fun testShouldMatchWithoutArgs() {
        setInput("""|lol
            |lollipop
            |Lol""".trimMargin().replace("\n", System.lineSeparator()))
        val grep = Grep(arrayOf("lol"))
        assertEquals("""|lol
            |lollipop""".trimMargin().replace("\n", System.lineSeparator()), grep.execute())
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
            |Lol""".trimMargin().replace("\n", System.lineSeparator()))
        val grep = Grep(arrayOf("lol", "-i"))
        assertEquals("""|lol
            |lollipop
            |Lol""".trimMargin().replace("\n", System.lineSeparator()), grep.execute())
    }

    @Test
    fun testShouldWriteAdditionalLines() {
        setInput("""|lol
            |kek
            |lollipop
            |Lol""".trimMargin().replace("\n", System.lineSeparator()))
        val grep = Grep(arrayOf("lol", "-wiA", "1"))
        assertEquals("""|lol
            |kek
            |Lol""".trimMargin().replace("\n", System.lineSeparator()), grep.execute())
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
        """.trimMargin().replace("\n", System.lineSeparator()))
        val grep = Grep(arrayOf("[0-9]+\\."))
        assertEquals("""|1. first
            |2. second
            |13.""".trimMargin().replace("\n", System.lineSeparator()), grep.execute())
    }

    @Test
    fun testShouldSupportWords() {
        setInput("hello, world!")
        val grep = Grep(arrayOf("hello", "-w"))
        assertEquals("hello, world!", grep.execute())
    }

    @Test
    fun testShouldCheckIllegalArgument() {
        setInput("hello, world!")
        val grep = Grep(arrayOf("hello", "-q"))
        try {
            grep.execute()
        } catch (e: CLIException) { }
    }

    @Test
    fun testShouldCheckIllegalArgumentType() {
        setInput("hello, world!")
        val grep = Grep(arrayOf("hello", "-A lol"))
        try {
            grep.execute()
        } catch (e: CLIException) { }
    }
}


class ParserTest {

    private val env = HashMap<String, String>()
    private val testFile = "src${File.separator}test${File.separator}resources${File.separator}test"

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

    @Test
    fun testParseInvalidPipe() {
        try {
            "echo 12 |".parseCommand(env)
        } catch (e: CLIException) {

        }
    }

    @Test
    fun testParseInvalidQuotes() {
        try {
            "echo 12 \"".parseCommand(env)
        } catch (e: CLIException) {

        }
    }

    @Test
    fun testShouldParseCatPipeGrep() {
        val command = "cat $filename | grep na -A 1".parseCommand(env)
        assertEquals("mama anarhia" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "stakan portveina", command?.execute())
    }

    @Test
    fun testShouldSupportParsingRegex() {
        setInput("1. first" + System.lineSeparator() +
            "still first" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "2. second" + System.lineSeparator() +
            "13." + System.lineSeparator() +
            "thirteenth" + System.lineSeparator() +
            "")
        val grep = "grep \"[0-9]+\\.\"".parseCommand(env)
        assertEquals("1. first" + System.lineSeparator() +
            "2. second" + System.lineSeparator() +
            "13.", grep?.execute())
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

    @Test
    fun testShouldParseCatPipeGrep() {
        val command = "cat $testFile | grep na -A 1".parseCommand(env)
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
        env["foo"] = "\"echo\""
        val command = "\$foo lol".parseCommand(env)
        assertEquals("lol", command?.execute())
    }
}