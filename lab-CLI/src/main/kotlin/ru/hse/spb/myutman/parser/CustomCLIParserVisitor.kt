package ru.hse.spb.myutman.parser

import ru.hse.spb.myutman.cli.*
import unquote

class CustomCLIParserVisitor(val env: MutableMap<String, String>) : CLIParserBaseVisitor<(Command?) -> Command?>() {
    override fun visitCommands(ctx: CLIParser.CommandsContext?): (Command?) -> Command? {
        return {
            var pipe: Command? = it
            for (command in ctx!!.command()) {
                pipe = visit(command)(pipe)
            }
            pipe
        }
    }

    override fun visitCommand(ctx: CLIParser.CommandContext?): (Command?) -> Command? {
        val commandName = ctx!!.IDENTIFIER().text
        val args = ctx.string().map { it.text.unquote() }.toTypedArray()
        return {
            when (commandName) {
                "echo"  -> Echo(args)
                "wc"    -> WC(args, it)
                "cat"   -> Cat(args, it)
                "pwd"   -> Pwd()
                else    -> BashCommand(commandName, args, it)
            }
        }
    }

    override fun visitAssignation(ctx: CLIParser.AssignationContext?): (Command?) -> Command? {
        val name = ctx!!.IDENTIFIER().text
        val value = ctx.string().text
        return {
            Assignation(name, value, env)
        }
    }

    override fun visitExit(ctx: CLIParser.ExitContext?): (Command?) -> Command? {
        return {
            Exit()
        }
    }
}