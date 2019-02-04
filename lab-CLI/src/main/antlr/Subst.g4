lexer grammar Subst;


//Recognizing quotes not to substitute into them
QUOTE : ('\'' .*? '\'') | ('"' .*? '"');

//Recognizing substitution
SUBST : '$'
        (('a'..'z')|('A'..'Z')|'_')
        (('a'..'z')|('A'..'Z')|('0'..'9')|'_')*
        ;

//Recognizing and skiping all other strings
STRING : .+? -> skip;