package ru.hse.spb.myutman.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

/**
 * Class for arguments parsed by ArgParser.
 */
class GrepArgs(parser: ArgParser) {
    val pattern by parser.positional(
        "PATTERN",
        help = "PATTERN to check lines")

    val ignore by parser.flagging(
        "-i",
        help = "ignore case difference"
    )

    val word by parser.flagging(
        "-w",
        help = "PATTERN should match the whole word"
    )

    val additionally by parser.storing(
        "-A",
        help = "additionally write n lines after any match"
    ) { toInt() }.default(0)
}