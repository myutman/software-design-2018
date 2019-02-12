package ru.hse.spb.myutman.cli

import com.xenomachina.argparser.ArgParser
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.system.exitProcess


private val defaultMap = mapOf(Pair("PWD", "."))

/**
 * CLI commands superclass.
 *
 * @property args command line arguments
 * @property pipe previous command in pipe
 */
abstract class Command(protected val args: Array<String> = emptyArray(), protected var pipe: Command? = null, protected val dict: Map<String, String> = defaultMap) {

    /**
     * Execute CLI command.
     */
    abstract fun execute() : String
}

/**
 * Command that prints its arguments.
 */
class Echo(args: Array<String> = emptyArray()) : Command(args) {
    override fun execute() : String {
        return args.joinToString(" ")
    }
}

private fun fileContents(fileName: String, dict: Map<String, String>): String {
    val fileInputStream = if (File(fileName).isAbsolute)
        FileInputStream(fileName)
    else
        FileInputStream(dict["PWD"] + File.separator + fileName)
    return fileContents(fileInputStream)
}

private fun fileContents(inputStream: InputStream): String {
    val reader = InputStreamReader(inputStream)
    return reader.readLines().joinToString("\n")
}

/**
 * Command that consistently prints contents of files listed in its arguments or if there are no any prints contents
 * given by pipe.
 */
class Cat(args: Array<String> = emptyArray(), pipe: Command? = null, dict: Map<String, String> = defaultMap) : Command(args, pipe, dict) {
    override fun execute() : String {
        return if (args.isEmpty()) {
            pipe ?. execute() ?: fileContents(System.`in`)
        } else {
            args.map { fileContents(it, dict) }.joinToString("\n")
        }
    }
}

/**
 * Command that consistently prints number of lines, words and characters in files listed in its arguments or if there
 * are no any prints number of lines, words and characters in contents given by pipe.
 */
class WC(args: Array<String> = emptyArray(), pipe: Command? = null, dict: Map<String, String> = defaultMap) : Command(args, pipe, dict) {

    private data class Result(val chars: Int, val words: Int, val lines: Int) {
        override fun toString(): String {
            return "\t$lines\t$words\t$chars"
        }

        operator fun plus (other: Result): Result{
            return Result(chars + other.chars, words + other.words, lines + other.lines)
        }
    }

    private fun wc(src: String): Result {
        val pattern = Pattern.compile("\\s+")
        return Result(src.length, src.split(pattern).filter { !it.isEmpty() }.size, src.split("\n").size)
    }

    override fun execute(): String {
        return if (args.isEmpty()) {
            wc(pipe ?. execute() ?: fileContents(System.`in`)).toString()
        } else {
            val results = args.map { wc(fileContents(it, dict)) }
            buildString {
                for ((name, res) in args.zip(results)) {
                    append(res, "\t", name, "\n")
                }
                append(results.reduce { acc, result ->  acc + result}, "\ttotal")
            }
        }
    }

}

/**
 * Command that prints absolute path of current directory.
 */
class Pwd(dict: Map<String, String>) : Command(dict = dict) {
    override fun execute(): String {
        return dict["PWD"]!!
    }

}

class Grep(args: Array<String> = emptyArray(), pipe: Command? = null): Command(args, pipe) {
    override fun execute(): String {
        val lines = (pipe ?. execute() ?: fileContents(System.`in`)).split("\n")
        return ArgParser(args).parseInto(::GrepArgs).run {
            val flags = if (ignore) Pattern.CASE_INSENSITIVE else 0
            val pat = if (word) {
                ".*(^|\\s)$pattern(\$|\\s).*"
            } else {
                ".*$pattern.*"
            }
            val regex = Pattern.compile(pat, flags).toRegex()

            val list = ArrayList<String>()
            var left = 0
            for (line in lines) {
                if (line.matches(regex)) {
                    list.add(line)
                    left = additionally
                } else if (left > 0) {
                    list.add(line)
                    left--
                }
            }
            list.joinToString("\n")
        }
    }
}

/**
 * Command that exits command line.
 */
class Exit : Command() {
    override fun execute(): String {
        exitProcess(0)
    }
}

/**
 * Command that adds variable into environment.
 *
 * @property name variable name
 * @property value variable new value
 * @property dict dictionary with environment variables
 */
class Assignation(private val name: String, private val value: String, private val env: MutableMap<String, String>) : Command(dict = env) {
    override fun execute(): String {
        env[name] = value
        return ""
    }
}

/**
 * All other commands executed by external bash with use of java.lang.Process.
 *
 * @property name name of bash command to execute
 */
class BashCommand(private val name: String, args: Array<String>, pipe: Command?) : Command(args, pipe) {
    override fun execute(): String {
        val runtime = Runtime.getRuntime().exec(buildString {
            append(name + " " + args.joinToString(" "))
        })
        val commandOutput = runtime.inputStream
        return fileContents(commandOutput)
    }
}