package ru.hse.spb.myutman.cli

import java.io.IOException
import kotlin.system.exitProcess

fun main() {

    // Gets external environment variables
    val env = HashMap(System.getenv())

    while (true) {
        // Prints command line tag
        print("hi there!$ ")
        System.out.flush()

        // Reads CLI command and parses it into ru.hse.spb.myutman.cli.Command instance
        val command: Command?
        try {
            command = readLine()?.parseCommand(env)
        } catch (e: IOException) {
            exitProcess(0)
        }

        try {
            // Prints result of command execution
            println(command?.execute() ?: "")
        } catch (e: CLIException) {
            System.err.println(e.message)
        }
    }
}