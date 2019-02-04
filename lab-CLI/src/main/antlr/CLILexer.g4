lexer grammar CLILexer;

PIPE : '|';
ASSIGN : '=';

//Exit in quotes is correct exit
EXIT : 'exit' | ('\'exit\'') | ('"exit"');

IDENTIFIER
    :   (('a'..'z')|('A'..'Z')|'_')(('a'..'z')|('A'..'Z')|('0'..'9')|'_')*
    ;

//Unary or binary quote
QUOTE : ('\'' ~('\'')*? '\'') | ('"' ~('"')*? '"');

//Skiping whitspaces
WS : (' ' | '\t' | '\r'| '\n') -> skip;

//Recognizing all other good strings
STRING : (~(' ' | '\t' | '\r' | '\n' | '\'' | '"' | '='))+;