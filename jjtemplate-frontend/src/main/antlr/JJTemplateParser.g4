parser grammar JJTemplateParser;

options {
    tokenVocab = JJTemplateLexer;
    language = Java;
}

template
    : (text | interpolation)* EOF
    ;

text
    : TEXT
    ;

interpolation
    : EXPR_OPEN   expression CLOSE          # ExprInterpolation
    | SPREAD_OPEN expression CLOSE          # SpreadInterpolation
    | COND_OPEN   expression CLOSE          # CondInterpolation
    ;

expression
    : pipeExpr
    | ternarExpression
    | primary
    | switchExpression
    | thenSwitchCaseExpression
    | elseSwitchCaseExpression
    | rangeExpression
    ;

ternarExpression
    : ternarExpressionCondition QUESTION ternarExpressionOnTrue COLON ternarExpressionOnFalse
    ;

ternarExpressionCondition
    : pipeExpr
    | primary
    ;

ternarExpressionOnTrue
    : primary
    ;

ternarExpressionOnFalse
    : primary
    ;

pipeExpr
    : primary (PIPE functionCall)+
    ;

primary
    : STRING
    | FLOAT_NUMBER
    | NUMBER
    | BOOLEAN
    | NULL
    | variable
    | functionCall
    | parenExpression
    ;

parenExpression
    : LPAREN expression RPAREN
    ;

variable
    : DOT segment (DOT segment)*
    ;

segment
    : name=IDENT (LPAREN argList? RPAREN)?
    ;

functionCall
    : (namespace=IDENT COLON)? functionName=IDENT argList?
    ;

argList
    : primary (COMMA primary)*
    ;

rangeExpression
    : name=IDENT KEYWORD_RANGE item=IDENT COMMA index=IDENT KEYWORD_OF collection=expression
    ;

switchExpression
    : name=IDENT KEYWORD_SWITCH expression
    | switchCase=primary KEYWORD_SWITCH expression
    ;

thenSwitchCaseExpression
    : KEYWORD_THEN (KEYWORD_SWITCH expression)?
    ;

elseSwitchCaseExpression
    : KEYWORD_ELSE (KEYWORD_SWITCH expression)?
    ;