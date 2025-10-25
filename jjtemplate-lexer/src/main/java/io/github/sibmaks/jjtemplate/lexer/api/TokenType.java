package io.github.sibmaks.jjtemplate.lexer.api;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;

/**
 * Defines all possible types of tokens recognized by the {@link TemplateLexer}.
 * <p>
 * Each {@code TokenType} corresponds to a specific syntactic element
 * within a template, such as literals, delimiters, or control symbols.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public enum TokenType {
    /**
     * Plain text outside any template expression.
     */
    TEXT,

    /**
     * Opening tag for a standard expression — {@code {{ ... }} }.
     */
    OPEN_EXPR,

    /**
     * Opening tag for a conditional expression — {@code {{? ... }} }.
     */
    OPEN_COND,

    /**
     * Opening tag for a spread expression — {@code {{. ... }} }.
     */
    OPEN_SPREAD,

    /**
     * Closing tag for any template expression — {@code }} }.
     */
    CLOSE,

    /**
     * Pipe operator — {@code |}, used to chain functions.
     */
    PIPE,

    /**
     * Dot operator — {@code .}, used for property or method access.
     */
    DOT,

    /**
     * Comma separator — {@code ,}, used to separate arguments.
     */
    COMMA,

    /**
     * Colon symbol — {@code :}.
     */
    COLON,

    /**
     * Question mark symbol — {@code ?}.
     */
    QUESTION,

    /**
     * Left parenthesis — {@code (}.
     */
    LPAREN,

    /**
     * Right parenthesis — {@code )}.
     */
    RPAREN,

    /**
     * String literal token.
     */
    STRING,

    /**
     * Numeric literal token.
     */
    NUMBER,

    /**
     * Boolean literal token — {@code true} or {@code false}.
     */
    BOOLEAN,

    /**
     * Null literal token — {@code null}.
     */
    NULL,

    /**
     * Identifier token — variable or function name.
     */
    IDENT,

    /**
     * Keyword token (reserved word recognized by the lexer).
     */
    KEYWORD

}