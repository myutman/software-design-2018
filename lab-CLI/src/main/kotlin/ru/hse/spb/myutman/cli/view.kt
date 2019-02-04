package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.myutman.parser.CLILexer
import ru.hse.spb.myutman.parser.CLIParser
import ru.hse.spb.myutman.parser.CustomCLIParserVisitor
import substitution

fun main() {
    val env = HashMap(System.getenv())

    while (true) {
        print("hi there!$ ")
        System.out.flush()
        val line = readLine()
            ?.substitution(env)
            ?:""

        val lexer = CLILexer(CharStreams.fromString(line))
        val parser = CLIParser(BufferedTokenStream(lexer))
        val visitor = CustomCLIParserVisitor(env)
        val command = visitor.visit(parser.line())
        println(command(null)?.execute()?:"lol")
    }
}