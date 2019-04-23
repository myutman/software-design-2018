package ru.hse.spb.myutman.parser

import ru.hse.spb.myutman.cli.*

/**
 * Custom visitor to parse CLI commands. Visit returns lambda that takes Command? as previous command in pipeline and
 * returns complete pipeline ends with current command.
 *
 * @property env dictionary with environment variables
 */
class CustomCLIParserVisitor(val env: MutableMap<String, String>) : CLIParserBaseVisitor<(Command?) -> Command?>() {

    /**
     * Visit all pipe separated commands and creates a pipeline of them
     *
     * @return lambda that takes command and adds it to the pipeline
     */
    override fun visitCommands(ctx: CLIParser.CommandsContext?): (Command?) -> Command? {
        return {
            // Put given command as a result
            var pipe: Command? = it
            for (command in ctx!!.command()) {
                // Use current result as a pipe for a new recognized command
                pipe = visit(command)(pipe)
            }
            pipe
        }
    }

    /**
     * Parses CLI command
     *
     * @return lambda to add into pipeline
     */
    override fun visitCommand(ctx: CLIParser.CommandContext?): (Command?) -> Command? {
        // Gets command name
        val commandName = ctx!!.commandName().text.unquote()

        // Gets command args
        val args = ctx.string().map { it.text.unquote() }.toTypedArray()

        return {
            // Switch on different situations of command names and use given Command? as pipe
            when (commandName.unquote()) {
                ""      ->  throw CLIException("error: empty command")
                "echo"  -> Echo(args)
                "wc"    -> WC(args, it, env)
                "cat"   -> Cat(args, it, env)
                "pwd"   -> Pwd(env)
                "grep"  -> Grep(args, it)
                "ls"    -> Ls(args, it, env)
                "cd"    -> Cd(args, env)
                else    -> BashCommand(commandName, args, env)
            }
        }
    }

    /**
     * Parses assignation
     *
     * @return lambda to add into pipeline
     */
    override fun visitAssignation(ctx: CLIParser.AssignationContext?): (Command?) -> Command? {
        // Gets key
        val name = ctx!!.IDENTIFIER().text

        //Gets value
        val value = ctx.string().text.unquote()

        return {
            Assignation(name, value, env)
        }
    }

    /**
     * Parses exit command
     *
     * @return lambda to add into pipeline
     */
    override fun visitExit(ctx: CLIParser.ExitContext?): (Command?) -> Command? {
        return {
            Exit()
        }
    }
}