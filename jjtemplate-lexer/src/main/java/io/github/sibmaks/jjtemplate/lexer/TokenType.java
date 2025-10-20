package io.github.sibmaks.jjtemplate.lexer;

/**
 * @author sibmaks
 */
public enum TokenType {
    TEXT,
    OPEN_EXPR, // '{{'
    OPEN_COND, // '{{?'
    OPEN_SPREAD, // '{{.'
    CLOSE, // '}}'
    PIPE, // '|'
    DOT, // '.'
    COMMA, // ','
    COLON, // ';'
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    IDENT,
    KEYWORD
}