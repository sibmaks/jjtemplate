package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Parser for template expressions from {@link TemplateLexer} tokens.</p>
 * Supports:
 * <ul>
 * <li>literals ({@code true}, {@code false}, {@code null}, numbers, strings)</li>
 * <li>variable access ({@code .a}, {@code .a.b.c})</li>
 * <li>function calls ({@code func arg1 arg2})</li>
 * <li>pipe expressions ({@code .value | str | upper})</li>
 * </ul>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateParser {

    private final List<Token> tokens;
    private int pos = 0;

    /**
     * Creates a new {@code TemplateParser} instance for the specified token list.
     *
     * @param tokens the list of tokens produced by {@link TemplateLexer}
     */
    public TemplateParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses a single expression from the current token stream.
     * <p>
     * Supports literals, variables, function calls, and pipe expressions.
     * </p>
     *
     * @return the parsed {@link Expression}
     * @throws TemplateParserException if an unexpected token or syntax error occurs
     */
    public Expression parseExpression() {
        var expr = parsePrimary();
        if (match(TokenType.PIPE)) {
            var chain = new ArrayList<FunctionCallExpression>();
            do {
                chain.add(parseFunctionCall());
            } while (match(TokenType.PIPE));
            expr = new PipeExpression(expr, chain);
        }
        // Ternary operator
        if (match(TokenType.QUESTION)) {
            var ifTrue = parseExpression(); // right after '?'
            expect(TokenType.COLON, ":");
            var ifFalse = parseExpression();
            expr = new TernaryExpression(expr, ifTrue, ifFalse);
        }
        return expr;
    }

    /**
     * Parses an entire template, which may include text literals and embedded expressions.
     * <p>
     * When multiple parts are found, they are combined into a {@code concat(...)} call.
     * </p>
     *
     * @return a composite {@link Expression} representing the parsed template
     * @throws TemplateParserException if the template structure is invalid or incomplete
     */
    public Expression parseTemplate() {
        var parts = new ArrayList<Expression>();

        while (pos < tokens.size()) {
            var t = peek();

            if (t == null) {
                throw error("Unexpected end of template");
            }

            switch (t.type) {
                case TEXT:
                    advance();
                    parts.add(new LiteralExpression(t.lexeme));
                    break;

                case OPEN_EXPR:
                case OPEN_COND:
                case OPEN_SPREAD:
                    advance();
                    var expr = parseExpression();
                    expect(TokenType.CLOSE, "}}");
                    parts.add(expr);
                    break;

                default:
                    throw error("Unexpected token outside expression: " + t.type);
            }
        }

        if (parts.isEmpty()) {
            throw error("Expected expression inside {{...}}, but template is empty");
        }

        if (parts.size() == 1) {
            return parts.get(0); // single literal or expression
        }

        // Build concat(...)
        return new FunctionCallExpression(
                "concat",
                parts
        );
    }

    /**
     * Parses a primary expression.
     * <p>
     * A primary expression can be a literal, variable, function call, or a parenthesized expression.
     * </p>
     *
     * @return the parsed {@link Expression}
     * @throws TemplateParserException if an unexpected token is encountered
     */
    private Expression parsePrimary() {
        var t = peek();
        if (t == null) {
            throw error("Unexpected end of expression");
        }
        switch (t.type) {
            case STRING:
                advance();
                return new LiteralExpression(t.lexeme);
            case NUMBER:
                advance();
                var num = t.lexeme;
                var isDecimal = num.contains(".");
                if (isDecimal) {
                    return new LiteralExpression(new BigDecimal(num));
                }
                return new LiteralExpression(Integer.parseInt(num));
            case BOOLEAN:
                advance();
                return new LiteralExpression(Boolean.valueOf(t.lexeme));
            case NULL:
                advance();
                return new LiteralExpression(null);
            case DOT:
                return parseVariable();
            case IDENT:
                return parseFunctionCallOrIdent();
            case LPAREN:
                advance();
                var inner = parseExpression();
                expect(TokenType.RPAREN, ")");
                return inner;
            default:
                throw error("Unexpected token: " + t.type);
        }
    }

    /**
     * Parses a variable expression starting with a dot (e.g., {@code .a.b.c}).
     *
     * @return the parsed {@link VariableExpression}
     * @throws TemplateParserException if the variable syntax is invalid
     */
    private VariableExpression parseVariable() {
        expect(TokenType.DOT, ".");
        var segments = new ArrayList<VariableExpression.Segment>();

        do {
            var ident = expect(TokenType.IDENT, "identifier after '.'");
            var name = ident.lexeme;
            var args = new ArrayList<Expression>();

            var method = match(TokenType.LPAREN);
            if (method) {
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RPAREN, ")");
            }

            if (method) {
                segments.add(new VariableExpression.Segment(name, args));
            } else {
                segments.add(new VariableExpression.Segment(name));
            }
        } while (match(TokenType.DOT));

        return new VariableExpression(segments);
    }

    /**
     * Parses either a function call or a standalone identifier expression.
     *
     * @return the parsed {@link FunctionCallExpression}
     * @throws TemplateParserException if the syntax is invalid
     */
    private FunctionCallExpression parseFunctionCallOrIdent() {
        var nameTok = expect(TokenType.IDENT, "function or identifier");
        var name = nameTok.lexeme;
        var args = parseArguments();
        return new FunctionCallExpression(name, args);
    }

    /**
     * Parses a function call following a pipe operator ({@code |}).
     *
     * @return the parsed {@link FunctionCallExpression}
     * @throws TemplateParserException if the function name or arguments are invalid
     */
    private FunctionCallExpression parseFunctionCall() {
        var nameTok = expect(TokenType.IDENT, "function after '|'");
        var name = nameTok.lexeme;
        var args = parseArguments();
        return new FunctionCallExpression(name, args);
    }

    /**
     * Parses a list of function arguments until a stop token is encountered.
     *
     * @return a list of parsed argument {@link Expression}s, possibly empty
     */
    private List<Expression> parseArguments() {
        var args = new ArrayList<Expression>();
        var t = peek();
        if (t == null || isStopToken(t)) {
            return args; // no args
        }
        do {
            args.add(parsePrimary());
        } while (match(TokenType.COMMA));
        return args;
    }

    /**
     * Determines whether the given token marks the end of an argument list or expression.
     *
     * @param t the token to check
     * @return {@code true} if the token is a stop token, {@code false} otherwise
     */
    private boolean isStopToken(Token t) {
        switch (t.type) {
            case QUESTION:
            case PIPE:
            case CLOSE:
            case COMMA:
            case RPAREN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return the current {@link Token}, or {@code null} if end of stream is reached
     */
    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    /**
     * Advances the parser to the next token.
     *
     * @return the previously current {@link Token}
     */
    private Token advance() {
        return tokens.get(pos++);
    }

    /**
     * Advances the parser to the next token.
     *
     * @return the previously current {@link Token}
     */
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Checks whether the current token matches the given type and consumes it if so.
     *
     * @param type the token type to match
     * @return {@code true} if the token was matched and consumed, {@code false} otherwise
     */
    private boolean check(TokenType type) {
        var t = peek();
        return t != null && t.type == type;
    }

    /**
     * Ensures that the current token matches the expected type.
     * If not, throws a {@link TemplateParserException}.
     *
     * @param type the expected token type
     * @param msg  the message to include in the exception if the expectation fails
     * @return the consumed {@link Token}
     * @throws TemplateParserException if the expected token is missing or of a different type
     */
    private Token expect(TokenType type, String msg) {
        var t = peek();
        if (t == null) {
            throw error("Expected " + msg + ", but reached end");
        }
        if (t.type != type) {
            throw error("Expected " + msg + ", got " + t.type);
        }
        advance();
        return t;
    }

    /**
     * Creates a new {@link TemplateParserException} with the given message and current position.
     *
     * @param msg the detailed error message
     * @return a new parser exception
     */
    private TemplateParserException error(String msg) {
        return new TemplateParserException(msg, pos);
    }

}
