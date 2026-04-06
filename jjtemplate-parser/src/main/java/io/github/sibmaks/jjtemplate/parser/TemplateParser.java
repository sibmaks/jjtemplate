package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.parser.api.Expression;

import java.util.List;

/**
 * Parser for template expressions based on {@link TemplateLexer} tokens.
 * <p>
 * Supports:
 * <ul>
 *   <li>literals ({@code true}, {@code false}, {@code null}, numbers, strings)</li>
 *   <li>variable access ({@code .a}, {@code .a.b.c})</li>
 *   <li>function calls ({@code func arg1 arg2})</li>
 *   <li>pipe expressions ({@code .value | str | upper})</li>
 *   <li>ternary operator ({@code cond ? a : b})</li>
 * </ul>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateParser {
    private final TemplateParserCursor cursor;
    private final TemplateExpressionParser expressionParser;

    /**
     * Creates a new parser for the given token list.
     *
     * @param tokens list of lexer tokens
     */
    public TemplateParser(List<Token> tokens) {
        this.cursor = new TemplateParserCursor(tokens);
        this.expressionParser = new TemplateExpressionParser(cursor);
    }

    /**
     * Parses a single expression from the current token stream.
     *
     * @return parsed expression
     */
    public Expression parseExpression() {
        return expressionParser.parseExpression();
    }

    /**
     * Ensures the parser has consumed all tokens.
     *
     * @throws TemplateParserException when unexpected tokens remain
     */
    public void expectEnd() {
        cursor.expectEnd();
    }
}
