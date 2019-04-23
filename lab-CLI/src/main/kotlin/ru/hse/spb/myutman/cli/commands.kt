package ru.hse.spb.myutman.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.MissingRequiredPositionalArgumentException
import com.xenomachina.argparser.UnrecognizedOptionException
import java.io.*
import java.nio.file.Paths
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
    abstract fun execute(): String
}

/**
 * Command that prints its arguments.
 */
class Echo(args: Array<String> = emptyArray()) : Command(args) {
    override fun execute(): String {
        return args.joinToString(" ")
    }
}

private fun fileContents(fileName: String, dict: Map<String, String>): String {
    val file = File(getFullPath(fileName, dict))
    val fileInputStream = FileInputStream(file)
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

        operator fun plus(other: Result): Result {
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
                    append(results.reduce { acc, result -> acc + result }, "\ttotal")
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
 * Use: grep PATTERN [-i] [-w] [-A n] [FILENAME ...]
 * Command that prints all lines that match PATTERN
 * -i: ignore letter case
 * -w: match whole words
 * -A n: additionally write n lines after any match
 * FILENAME: additionally use lines from file with given name
 */
class Grep(args: Array<String> = emptyArray(),
           pipe: Command? = null):
    Command(args, pipe) {

    override fun execute(): String {
        try {
            return ArgParser(args).parseInto(::GrepArgs).run {
                val lines = (
                        if (!fileName.isEmpty()) fileContents(fileName, dict)
                        else (pipe?.execute() ?: fileContents(System.`in`))
                    ).split(System.lineSeparator())

                val flags = if (ignore) Pattern.CASE_INSENSITIVE else 0
                val pat = if (word) {
                    ".*\\b$pattern\\b.*"
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
                list.joinToString(System.lineSeparator())
            }
        } catch (e: MissingRequiredPositionalArgumentException) {
            throw CLIException("grep: ${e.message}")
        } catch (e: UnrecognizedOptionException) {
            throw CLIException("grep: ${e.message}")
        } catch (e: IOException) {
            throw CLIException("grep: ${e.message}")
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
class Assignation(private val name: String,
                  private val value: String,
                  private val env: MutableMap<String, String>)
    : Command(dict = env) {

    override fun execute(): String {
        if (name != "PWD") {
            env[name] = value
        } else {
            throw CLIException("You can't change PWD variable value")
        }
        return ""
    }
}


class Ls(args: Array<String> = emptyArray(), pipe: Command? = null, dict: Map<String, String> = defaultMap) :
    Command(args, pipe, dict) {
    override fun execute(): String {
        return if (args.isEmpty()) {
            try {
                val res = pipe?.execute() ?: ""
                val path = if (res == "") {
                    dict["PWD"] ?: "."
                } else {
                    res
                }
                lsDir(getFullPath(path, dict))
            } catch (e: IOException) {
                throw CLIException("ls: ${e.message}")
            }
        } else {
            try {
                if (args.size == 1) {
                    return lsDir(getFullPath(args[0], dict))
                }
                val stringBuilder = StringBuilder()
                for (arg in args) {
                    stringBuilder.append(" $arg:${System.lineSeparator()}${lsDir(getFullPath(arg, dict))}")
                }
                stringBuilder.toString()
            } catch (e: IOException) {
                throw CLIException("ls: ${e.message}")
            }
        }
    }

    private fun lsDir(dirName: String): String {
        val dir = File(dirName)
        if (dir.exists() && dir.isDirectory) {
            val stringBuilder = StringBuilder()
            for (file in dir.listFiles()) {
                if (file.isDirectory) {
                    stringBuilder.append("dir: ${file.name}${System.lineSeparator()}")
                } else {
                    stringBuilder.append("file: ${file.name}${System.lineSeparator()}")
                }
            }
            return stringBuilder.toString()
        } else {
            throw CLIException("ls: There is no such directory")
        }
    }
}

class Cd(args: Array<String> = emptyArray(), private val env: MutableMap<String, String> = mutableMapOf()) :
    Command(args, null, env) {
    override fun execute(): String {
        if (args.size > 1) {
            throw CLIException("cd: More than 1 argument given")
        }
        val dir = if (args.isEmpty()) System.getProperty("user.home") else args[0]
        val newPwd = getFullPath(dir, env)
        val file = File(newPwd)
        if (file.exists() && file.isDirectory) {
            env["PWD"] = newPwd
            return ""
        } else {
            throw CLIException("cd: There is no such directory")
        }
    }
}

/**
 * All other commands executed by external bash with use of java.lang.Process.
 *
 * @property name name of bash command to execute
 */
class BashCommand(private val name: String,
                  args: Array<String>,
                  env: MutableMap<String, String>)
    : Command(args, null, env) {

    override fun execute(): String {
        val runtime: Process
        try {
            runtime = Runtime.getRuntime().exec(buildString {
                append(name + " " + args.joinToString(" "))
            })
        } catch (e: IOException) {
            throw CLIException("$name: ${e.message}")
        }
        val code = runtime.waitFor()
        if (code != 0) {
            val message = fileContents(runtime.errorStream)
            throw CLIException(message)
        }
        val commandOutput = runtime.inputStream
        try {
            val res = fileContents(commandOutput)
            return res
        } catch (e: IOException) {
            throw CLIException("$name: ${e.message}")
        }
    }
}

fun getFullPath(fileName: String, env: Map<String, String>): String {
    return Paths.get(env["PWD"]).resolve(fileName).normalize().toString()
}