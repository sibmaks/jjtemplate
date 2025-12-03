lexer grammar JJTemplateLexer;

// ---------------- DEFAULT MODE: OBJECT KEY ----------------

EXPR_OPEN
    : '{{'    -> pushMode(EXPRESSION_MODE)
    ;

SPREAD_OPEN
    : '{{.'   -> pushMode(EXPRESSION_MODE)
    ;

COND_OPEN
    : '{{?'   -> pushMode(EXPRESSION_MODE)
    ;

TEXT
    : ( '{' ~'{' | ~'{' )+
    ;

// ---------------- EXPRSSION MODE ----------------

mode EXPRESSION_MODE;

CLOSE
    : '}}'    -> popMode
    ;

KEYWORD_RANGE
    : 'range'
    ;

KEYWORD_OF
    : 'of'
    ;

KEYWORD_THEN
    : 'then'
    ;

KEYWORD_ELSE
    : 'else'
    ;

KEYWORD_SWITCH
    : 'switch'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

STRING
    : '\'' ( ESC | ~['\\] )* '\''
    ;

fragment ESC
    : '\\' [btnrf'"\\]    // \b \t \n \r \f \" \\
    ;

NUMBER
    : '-'? DIGIT+
    ;

FLOAT_NUMBER
    : '-'? DIGIT+ '.' DIGIT+
    ;

BOOLEAN
    : TRUE | FALSE
    ;

TRUE  : 'true'  ;
FALSE : 'false' ;
NULL  : 'null'  ;

DOT      : '.' ;
COMMA    : ',' ;
COLON    : ':' ;
QUESTION : '?' ;
PIPE     : '|' ;
LPAREN   : '(' ;
RPAREN   : ')' ;

IDENT
    : [A-Za-z] [A-Za-z0-9_]*
    ;

fragment DIGIT : [0-9] ;
