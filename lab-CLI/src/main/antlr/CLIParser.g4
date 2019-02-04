parser grammar CLIParser;

options { tokenVocab=CLILexer; }

line
    : assignation | exit | commands
    ;

commands
    : command (PIPE command)*
    ;

assignation
    : IDENTIFIER ASSIGN string
    ;

exit
    : EXIT
    ;

command
    : IDENTIFIER (string)*
    ;

string
    : IDENTIFIER | STRING | QUOTE
    ;


