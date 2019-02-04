lexer grammar InquoteSubst;

//Substitution inside binary quotes

//Recognizing substitution
SUBST : '$'
        (('a'..'z')|('A'..'Z')|'_')
        (('a'..'z')|('A'..'Z')|('0'..'9')|'_')*
        ;

//Recognizing and skiping all other strings
STRING : .+? -> skip;