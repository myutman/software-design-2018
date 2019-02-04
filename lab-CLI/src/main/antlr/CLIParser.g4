parser grammar CLIParser;

options { tokenVocab=CLILexer; }

//Command line possibly assignation, exit or sequence of piped commands
line
    : assignation | exit | commands
    ;

//Piped commands
commands
    : command (PIPE command)*
    ;

//Assign value to key
assignation
    : IDENTIFIER ASSIGN string
    ;

//Exit
exit
    : EXIT
    ;

//Command and then arguments
command
    : commandName (string)*
    ;

//Command name possibly identifier or quote
commandName
    : IDENTIFIER | QUOTE
    ;

//Argument
string
    : IDENTIFIER | STRING | QUOTE | EXIT
    ;
