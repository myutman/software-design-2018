package ru.hse.spb.myutman.cli

import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main() {

    // Gets external environment variables
    val env = HashMap(System.getenv())
    env["PWD"] = File("").absolutePath

    while (true) {
        // Prints command line tag
        print("hi there!$ ")
        System.out.flush()

        try {
            // Reads CLI command and parses it into ru.hse.spb.myutman.cli.Command instance
            val command = readLine()?.parseCommand(env)

            // Prints result of command execution
            println(command?.execute() ?: "")
        } catch (e: CLIException) {
            System.err.println(e.message)
        } catch (e: IOException) {
            exitProcess(0)
        }
    }
}