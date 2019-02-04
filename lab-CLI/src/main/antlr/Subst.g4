lexer grammar Subst;

QUOTE : ('\'' .*? '\'') | ('"' .*? '"');

SUBST : '$'
        (('a'..'z')|('A'..'Z')|'_')
        (('a'..'z')|('A'..'Z')|('0'..'9')|'_')*
        ;

STRING : .+? -> skip;