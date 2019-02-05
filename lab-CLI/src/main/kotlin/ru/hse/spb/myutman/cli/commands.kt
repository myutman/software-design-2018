package ru.hse.spb.myutman.cli

import java.io.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

/**
 * CLI commands superclass.
 *
 * @property args command line arguments
 * @property pipe previous command in pipe
 */
abstract class Command(protected val args: Array<String> = emptyArray(), protected var pipe: Command? = null) {

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

private fun fileContents(fileName: String): String {
    return fileContents(FileInputStream(fileName))
}

private fun fileContents(inputStream: InputStream): String {
    val reader = InputStreamReader(inputStream)
    return reader.readLines().joinToString("\n")
}

/**
 * Command that consistently prints contents of files listed in its arguments or if there are no any prints contents
 * given by pipe.
 */
class Cat(args: Array<String> = emptyArray(), pipe: Command? = null) : Command(args, pipe) {
    override fun execute() : String {
        return if (args.isEmpty()) {
            pipe ?. execute() ?: fileContents(System.`in`)
        } else {
            args.map { fileContents(it) }.joinToString("\n")
        }
    }
}

/**
 * Command that consistently prints number of lines, words and characters in files listed in its arguments or if there
 * are no any prints number of lines, words and characters in contents given by pipe.
 */
class WC(args: Array<String> = emptyArray(), pipe: Command? = null) : Command(args, pipe) {

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
            val results = args.map { wc(fileContents(it)) }
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
class Pwd : Command() {
    override fun execute(): String {
        return File(".").absolutePath
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
class Assignation(val name: String, val value: String, val dict: MutableMap<String, String>) : Command() {
    override fun execute(): String {
        dict[name] = value
        return ""
    }
}

/**
 * All other commands executed by external bash with use of java.lang.Process.
 *
 * @property name name of bash command to execute
 */
class BashCommand(val name: String, args: Array<String>, pipe: Command?) : Command(args, pipe) {
    override fun execute(): String {
        val runtime = Runtime.getRuntime().exec(buildString {
            append(name + " " + args.joinToString(" "))
        })
        val commandOutput = runtime.inputStream
        return fileContents(commandOutput)
    }
}