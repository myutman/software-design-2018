package ru.hse.spb.myutman.cli

fun main() {

    // Gets external environment variables
    val env = HashMap(System.getenv())

    while (true) {
        // Prints command line tag
        print("hi there!$ ")
        System.out.flush()

        // Reads CLI command and parses it into ru.hse.spb.myutman.cli.Command instance
        val command = readLine()?.parseCommand(env)

        // Prints result of command execution
        println(command?.execute()?:"")
    }
}