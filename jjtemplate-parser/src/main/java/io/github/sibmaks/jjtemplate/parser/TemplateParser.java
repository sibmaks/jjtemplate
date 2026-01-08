package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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

    private final List<Token> tokens;
    private int pos = 0;

    /**
     * Creates a new parser for the given token list.
     *
     * @param tokens list of lexer tokens
     */
    public TemplateParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses a single expression from the current token stream.
     * <p>
     * Supports literals, variables, function calls,
     * pipe expressions, and ternary expressions.
     *
     * @return parsed expression
     */
    public Expression parseExpression() {
        if (matchKeyword(Keyword.THEN)) {
            return parseThenSwitchCaseExpression();
        }
        if (matchKeyword(Keyword.ELSE)) {
            return parseElseSwitchCaseExpression();
        }
        if (check(TokenType.IDENT) && checkNextKeyword(Keyword.SWITCH)) {
            var nameToken = advance();
            expectKeyword(Keyword.SWITCH, "switch");
            var condition = parseExpression();
            return new SwitchExpression(new LiteralExpression(nameToken.lexeme), condition);
        }
        if (check(TokenType.IDENT) && checkNextKeyword(Keyword.RANGE)) {
            var nameToken = advance();
            expectKeyword(Keyword.RANGE, "range");
            return parseRangeExpression(new LiteralExpression(nameToken.lexeme));
        }

        var expr = parsePrimary();

        if (matchKeyword(Keyword.SWITCH)) {
            var condition = parseExpression();
            return new SwitchExpression(expr, condition);
        }
        if (matchKeyword(Keyword.RANGE)) {
            return parseRangeExpression(expr);
        }
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
     * Ensures the parser has consumed all tokens.
     *
     * @throws TemplateParserException when unexpected tokens remain
     */
    public void expectEnd() {
        if (pos < tokens.size()) {
            var token = peek();
            if (token == null) {
                throw error("Excepted end of expression");
            }
            throw error("Unexpected token: " + token.type);
        }
    }

    /**
     * Parses a primary expression such as:
     * <ul>
     *   <li>literals</li>
     *   <li>variables</li>
     *   <li>function calls</li>
     *   <li>parenthesized expressions</li>
     * </ul>
     *
     * @return parsed expression
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
                return num.contains(".")
                        ? new LiteralExpression(new BigDecimal(num))
                        : new LiteralExpression(new BigInteger(num));
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

    private Expression parseThenSwitchCaseExpression() {
        if (matchKeyword(Keyword.SWITCH)) {
            var condition = parseExpression();
            return new ThenSwitchCaseExpression(condition);
        }
        return new ThenSwitchCaseExpression(null);
    }

    private Expression parseElseSwitchCaseExpression() {
        if (matchKeyword(Keyword.SWITCH)) {
            var condition = parseExpression();
            return new ElseSwitchCaseExpression(condition);
        }
        return new ElseSwitchCaseExpression(null);
    }

    private Expression parseRangeExpression(Expression nameExpression) {
        var item = expect(TokenType.IDENT, "range item name");
        expect(TokenType.COMMA, ",");
        var index = expect(TokenType.IDENT, "range index name");
        expectKeyword(Keyword.OF, "of");
        var source = parseExpression();
        return new RangeExpression(nameExpression, item.lexeme, index.lexeme, source);
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

            var isCall = match(TokenType.LPAREN);
            if (isCall) {
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RPAREN, ")");
            }

            segments.add(
                    isCall
                            ? new VariableExpression.Segment(name, args)
                            : new VariableExpression.Segment(name)
            );
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
        var identToken = expect(TokenType.IDENT, "function or identifier");
        return parseSystemFunction(identToken);
    }

    /**
     * Parses a function call following a pipe operator ({@code |}).
     *
     * @return the parsed {@link FunctionCallExpression}
     * @throws TemplateParserException if the function name or arguments are invalid
     */
    private FunctionCallExpression parseFunctionCall() {
        var identToken = expect(TokenType.IDENT, "function after '|'");
        return parseSystemFunction(identToken);
    }

    /**
     * Parses a system-level function call using the initial identifier token.
     * <p>
     * Supports two forms:
     * <ul>
     *   <li>{@code namespace:function arg1 arg2}</li>
     *   <li>{@code function arg1 arg2}</li>
     * </ul>
     * If a namespace prefix is present, it must be followed by a colon.
     * Arguments are parsed as primary expressions.
     *
     * @param identToken the initial identifier token representing either the function name
     *                   or the namespace in a namespaced call
     * @return a {@link FunctionCallExpression} representing the parsed function call
     * @throws TemplateParserException if the function name or arguments are invalid
     */
    private FunctionCallExpression parseSystemFunction(Token identToken) {
        if (check(TokenType.COLON)) {
            advance();
            var functionName = expect(TokenType.IDENT, "function name required");
            var args = parseArguments();
            return new FunctionCallExpression(identToken.lexeme, functionName.lexeme, args);
        }
        var name = identToken.lexeme;
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
            if (check(TokenType.DOT)) {
                if (checkNext(1, TokenType.DOT)) {
                    if (checkNext(2, TokenType.DOT)) {
                        advance();
                        advance();
                        advance();
                        var argExpression = parsePrimary();
                        args.add(new SpreadExpression(argExpression));
                        continue;
                    }
                }
            }
            var argExpression = parsePrimary();
            args.add(argExpression);
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

    private boolean checkNextKeyword(Keyword keyword) {
        var next = peekNext(1);
        return next != null && next.type == TokenType.KEYWORD && keyword.eq(next.lexeme);
    }

    private Token peekNext(int offset) {
        return pos + offset < tokens.size() ? tokens.get(pos + offset) : null;
    }

    private boolean checkKeyword(Keyword keyword) {
        var token = peek();
        return token != null && token.type == TokenType.KEYWORD && keyword.eq(token.lexeme);
    }

    private boolean matchKeyword(Keyword keyword) {
        if (checkKeyword(keyword)) {
            advance();
            return true;
        }
        return false;
    }

    private void expectKeyword(Keyword keyword, String expected) {
        if (checkKeyword(keyword)) {
            advance();
            return;
        }
        throw error("Expected keyword '" + expected + "'");
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

    private boolean checkNext(int offset, TokenType type) {
        var t = peekNext(offset);
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
