import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.myutman.parser.InquoteSubst
import ru.hse.spb.myutman.parser.Subst

private fun String.inquoteSubstitution(dict: Map<String, String>): String {
    val lexer = InquoteSubst(CharStreams.fromString(this))
    var ans = this
    for (token in lexer.allTokens.reversed()) {
        ans = ans.replaceRange(token.startIndex..token.stopIndex, dict[token.text.substring(1)] ?. unquote() ?: "")
    }
    return ans
}

fun String.substitution(dict: Map<String, String>): String {
    val lexer = Subst(CharStreams.fromString(this))
    var ans = this
    for (token in lexer.allTokens.reversed()) {
        when (token.type) {
            Subst.SUBST -> ans = ans.replaceRange(token.startIndex..token.stopIndex, dict[token.text.substring(1)] ?: "")
            Subst.QUOTE -> {
                if (token.text[0] == '"') {
                    val substitute = token.text.substring(1..token.text.length - 2).inquoteSubstitution(dict)
                    ans = ans.replaceRange(token.startIndex + 1..token.stopIndex - 1, substitute)
                }
            }
        }
    }
    return ans
}

fun String.unquote(): String {
    return if (this[0] == '\'' || this[0] == '"')
        this.substring(1..length - 2)
    else
        this
}

