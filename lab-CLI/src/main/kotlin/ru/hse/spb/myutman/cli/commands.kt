package ru.hse.spb.myutman.cli

import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.system.exitProcess

abstract class Command(protected val args: Array<String> = emptyArray(), protected var pipe: Command? = null) {
    abstract fun execute() : String
}

class Echo(args: Array<String> = emptyArray()) : Command(args) {
    override fun execute() : String {
        return args.joinToString(" ") + "\n"
    }
}

private fun fileContents(fileName: String) : String {
    val reader = FileReader(fileName)
    return buildString {
        reader.forEachLine {
            append(it, "\n")
        }
    }
}

class Cat(args: Array<String> = emptyArray(), pipe: Command? = null) : Command(args, pipe) {
    override fun execute() : String {
        return if (args.isEmpty()) {
            pipe ?. execute() ?: buildString {
                InputStreamReader(System.`in`).forEachLine {
                    append(it, "\n")
                }
            }
        } else {
            args.map { fileContents(it) }.joinToString("\n")
        }
    }
}

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
        val pattern = Pattern.compile("\\w")
        return Result(src.length, src.split(pattern).filter { !it.isEmpty() }.size, src.split("\n").size)
    }

    override fun execute(): String {
        return if (args.isEmpty()) {
            wc(pipe ?. execute() ?: buildString {
                InputStreamReader(System.`in`).forEachLine {
                    append(it, '\n')
                }
            }).toString()
        } else {
            val results = args.map { wc(fileContents(it)) }
            buildString {
                for ((name, res) in args.zip(results)) {
                    append(res, "\t", name, "\n")
                }
                append(results.reduce { acc, result ->  acc + result}, "\tat all\n")
            }
        }
    }

}

class Pwd : Command() {
    override fun execute(): String {
        return File(".").absolutePath
    }

}

class Exit : Command() {
    override fun execute(): String {
        exitProcess(0)
    }
}

class Assignation(val name: String, val value: String, val dict: MutableMap<String, String>) : Command() {
    override fun execute(): String {
        dict[name] = value
        return ""
    }
}

class BashCommand(val name: String, args: Array<String>, pipe: Command?) : Command(args, pipe) {
    override fun execute(): String {
        val runtime = Runtime.getRuntime().exec(buildString {
            append(name + " " + args.joinToString(" "))
        })
        val commandOutput = runtime.inputStream
        return buildString {
            InputStreamReader(commandOutput).forEachLine {
                append(it + "\n")
            }
        }
    }
}