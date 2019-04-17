package ru.hse.spb.myutman.cli

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.myutman.parser.*

/**
 * Replace all the substitutions with their values from the environment dictionary in case we need to prepare it inside
 * double quotes
 */
private fun String.inQuoteSubstitution(dict: Map<String, String>): String {

    // Breaks line into lexemes
    val lexer = InquoteSubst(CharStreams.fromString(this))
    var ans = this

    // Reversed order is necessary not to invalidate indexes
    for (token in lexer.allTokens.reversed()) {
        // This token's type is substitution so we need to replace it in text with its value
        ans = ans.replaceRange(token.startIndex..token.stopIndex, dict[token.text.substring(1)] ?. unquote() ?: "")
    }
    return ans
}

/**
 * Replace all the substitutions with their values from the environment dictionary
 *
 * @param dict dictionary with environment variables
 */
fun String.substitution(dict: Map<String, String>): String {

    // Breaks line into lexemes
    val lexer = Subst(CharStreams.fromString(this))

    // This is variable we should change to get the answer
    var ans = this

    // Reversed order is necessary not to invalidate indexes
    for (token in lexer.allTokens.reversed()) {
        when (token.type) {
            // If token type is substitution then we need to replace it in text with its value
            Subst.SUBST -> ans = ans.replaceRange(token.startIndex..token.stopIndex,
                dict[token.text.substring(1)] ?: "")

            // If token is quote in double quotes then we need to use inQuoteSubstitution
            Subst.QUOTE -> {
                if (token.text[0] == '"') {
                    val substitute = token.text.unquote().inQuoteSubstitution(dict)
                    // Replaces text in double quotes with substitute
                    ans = ans.replaceRange(token.startIndex + 1..token.stopIndex - 1, substitute)
                }
            }
        }
    }
    return ans
}

/**
 * Return String with removed quotes if there are any
 */
fun String.unquote(): String {
    return if (!this.isEmpty() && (this[0] == '\'' || this[0] == '"'))
        this.substring(1..length - 2)
    else
        this
}

/**
 * Parses CLI command.
 *
 * @param env dictionary with environment variables
 */
fun String?.parseCommand(env: MutableMap<String, String>): Command? {
    // Prepares substitution
    val line = this?.substitution(env)?:""

    // Breaks line with substituted values into lexemes
    val lexer = CLILexer(CharStreams.fromString(line))

    // Builds AST on this lexemes
    val parser = CLIParser(BufferedTokenStream(lexer))

    // Parses command from AST
    val visitor = CustomCLIParserVisitor(env)
    val result: Command?
    try {
        result = visitor.visit(parser.line())(null)
    } catch (e: StringIndexOutOfBoundsException) {
        throw CLIException("error: unexpected end of line")
    }
    return result
}