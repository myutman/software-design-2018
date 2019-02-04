lexer grammar CLILexer;

PIPE : '|';
ASSIGN : '=';
EXIT : 'exit';

QUOTE : ('\'' ~('\'')*? '\'') | ('"' ~('"')*? '"');

IDENTIFIER
    :   (('a'..'z')|('A'..'Z')|'_')
        (('a'..'z')|('A'..'Z')|('0'..'9')|'_')*
;

WS : (' ' | '\t' | '\r'| '\n') -> skip;
STRING : (~(' ' | '\t' | '\r' | '\n' | '\'' | '"' | '='))+;