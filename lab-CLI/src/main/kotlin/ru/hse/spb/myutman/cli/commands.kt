package ru.hse.spb.myutman.cli

import java.io.*
import java.util.regex.Pattern
import kotlin.system.exitProcess


private val defaultMap = mapOf(Pair("PWD", "."))

/**
 * CLI commands superclass.
 *
 * @property args command line arguments
 * @property pipe previous command in pipe
 */
abstract class Command(protected val args: Array<String> = emptyArray(),
                       protected var pipe: Command? = null,
                       protected val dict: Map<String, String> = defaultMap) {

    /**
     * Execute CLI command.
     */
    @Throws(CLIException::class)
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

@Throws(IOException::class)
private fun fileContents(inputStream: InputStream): String {
    val reader = InputStreamReader(inputStream)
    return reader.readLines().joinToString(System.lineSeparator())
}

/**
 * Command that consistently prints contents of files listed in its arguments or if there are no any prints contents
 * given by pipe.
 */
class Cat(args: Array<String> = emptyArray(),
          pipe: Command? = null,
          dict: Map<String, String> = defaultMap)
    : Command(args, pipe, dict) {

    override fun execute() : String {
        return if (args.isEmpty()) {
            try {
                pipe?.execute() ?: fileContents(System.`in`)
            } catch (e: IOException) {
                throw CLIException("cat: ${e.message}")
            }
        } else {
            try {
                args.map { fileContents(it, dict) }.joinToString(System.lineSeparator())
            } catch (e: IOException) {
                throw CLIException("cat: ${e.message}")
            }
        }
    }
}

/**
 * Command that consistently prints number of lines, words and characters in files listed in its arguments or if there
 * are no any prints number of lines, words and characters in contents given by pipe.
 */
class WC(args: Array<String> = emptyArray(),
         pipe: Command? = null,
         dict: Map<String, String> = defaultMap)
    : Command(args, pipe, dict) {

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
        return Result(src.length,
            src.split(pattern).filter { !it.isEmpty() }.size,
            src.split(System.lineSeparator()).size
        )
    }

    override fun execute(): String {
        return if (args.isEmpty()) {
            try {
                wc(pipe?.execute() ?: fileContents(System.`in`)).toString()
            } catch (e: IOException) {
                throw CLIException("wc: ${e.message}")
            }
        } else {
            try {
                val results = args.map { wc(fileContents(it, dict)) }
                buildString {
                    for ((name, res) in args.zip(results)) {
                        append(res, "\t", name, System.lineSeparator())
                    }
                    append(results.reduce { acc, result ->  acc + result}, "\ttotal")
                }
            } catch (e: IOException) {
                throw CLIException("wc: ${e.message}")
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
class Assignation(private val name: String,
                  private val value: String,
                  private val env: MutableMap<String, String>)
    : Command(dict = env) {

    override fun execute(): String {
        if (!name.equals("PWD")) {
            env[name] = value
        }
        return ""
    }
}

/**
 * All other commands executed by external bash with use of java.lang.Process.
 *
 * @property name name of bash command to execute
 */
class BashCommand(private val name: String,
                  args: Array<String>,
                  pipe: Command?)
    : Command(args, pipe) {

    override fun execute(): String {
        val runtime = Runtime.getRuntime().exec(buildString {
            append(name + " " + args.joinToString(" "))
        })
        val commandOutput = runtime.inputStream
        try {
            val res = fileContents(commandOutput)
            return res
        } catch (e: IOException) {
            throw CLIException("$name: ${e.message}")
        }
    }
}