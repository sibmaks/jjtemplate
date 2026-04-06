package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class TemplateExpressionParser {
    private final TemplateParserCursor cursor;

    TemplateExpressionParser(TemplateParserCursor cursor) {
        this.cursor = cursor;
    }

    Expression parseExpression() {
        return parseSpecialExpression();
    }

    private Expression parseSpecialExpression() {
        if (cursor.matchKeyword(Keyword.THEN)) {
            return parseThenSwitchCaseExpression();
        }
        if (cursor.matchKeyword(Keyword.ELSE)) {
            return parseElseSwitchCaseExpression();
        }
        if (cursor.check(TokenType.IDENT) && cursor.checkNextKeyword(Keyword.SWITCH)) {
            var nameToken = cursor.advance();
            cursor.expectKeyword(Keyword.SWITCH, "switch");
            var condition = parseExpression();
            return new SwitchExpression(new LiteralExpression(nameToken.lexeme), condition);
        }
        if (cursor.check(TokenType.IDENT) && cursor.checkNextKeyword(Keyword.RANGE)) {
            var nameToken = cursor.advance();
            cursor.expectKeyword(Keyword.RANGE, "range");
            return parseRangeExpression(new LiteralExpression(nameToken.lexeme));
        }
        return parseTernaryExpression();
    }

    private Expression parseTernaryExpression() {
        var expression = parsePipeExpression();
        if (!cursor.match(TokenType.QUESTION)) {
            return expression;
        }
        var ifTrue = parseExpression();
        cursor.expect(TokenType.COLON, ":");
        var ifFalse = parseExpression();
        return new TernaryExpression(expression, ifTrue, ifFalse);
    }

    private Expression parsePipeExpression() {
        var expression = parsePostfixKeywordExpression();
        if (!cursor.match(TokenType.PIPE)) {
            return expression;
        }

        var chain = new ArrayList<FunctionCallExpression>();
        do {
            chain.add(parseFunctionCall());
        } while (cursor.match(TokenType.PIPE));
        return new PipeExpression(expression, chain);
    }

    private Expression parsePostfixKeywordExpression() {
        var expression = parsePrimary();
        if (cursor.matchKeyword(Keyword.SWITCH)) {
            var condition = parseExpression();
            return new SwitchExpression(expression, condition);
        }
        if (cursor.matchKeyword(Keyword.RANGE)) {
            return parseRangeExpression(expression);
        }
        return expression;
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
        var token = cursor.peek();
        if (token == null) {
            throw cursor.error("Unexpected end of expression");
        }
        switch (token.type) {
            case STRING:
                cursor.advance();
                return new LiteralExpression(token.lexeme);
            case NUMBER:
                return parseNumberLiteral(token);
            case BOOLEAN:
                cursor.advance();
                return new LiteralExpression(Boolean.valueOf(token.lexeme));
            case NULL:
                cursor.advance();
                return new LiteralExpression(null);
            case DOT:
                return parseVariable();
            case IDENT:
                return parseFunctionCallOrIdent();
            case LPAREN:
                return parseParenthesizedExpression();
            case OPEN_EXPR:
            case OPEN_COND:
            case OPEN_SPREAD:
                return parseNestedInterpolation();
            default:
                throw cursor.error("Unexpected token: " + token.type);
        }
    }

    private LiteralExpression parseNumberLiteral(Token token) {
        cursor.advance();
        var number = token.lexeme;
        return number.contains(".")
                ? new LiteralExpression(new BigDecimal(number))
                : new LiteralExpression(new BigInteger(number));
    }

    private Expression parseParenthesizedExpression() {
        cursor.advance();
        var inner = parseExpression();
        cursor.expect(TokenType.RPAREN, ")");
        return inner;
    }

    private Expression parseNestedInterpolation() {
        var openToken = cursor.advance();
        if (openToken.type != TokenType.OPEN_EXPR) {
            throw cursor.error("Only '{{ ... }}' is allowed in nested interpolation");
        }

        var start = cursor.position();
        var depth = 0;
        var end = -1;
        while (cursor.hasMore()) {
            var token = cursor.peek();
            if (token == null) {
                break;
            }
            if (token.type == TokenType.OPEN_EXPR
                    || token.type == TokenType.OPEN_COND
                    || token.type == TokenType.OPEN_SPREAD) {
                depth++;
                cursor.advance();
                continue;
            }
            if (token.type == TokenType.CLOSE) {
                if (depth == 0) {
                    end = cursor.position();
                    break;
                }
                depth--;
            }
            cursor.advance();
        }

        if (end < 0) {
            throw cursor.error("Missing closing '}}'");
        }

        var parser = new TemplateParser(cursor.tokens().subList(start, end));
        var expression = parser.parseExpression();
        parser.expectEnd();
        cursor.setPosition(end + 1);
        return expression;
    }

    private Expression parseThenSwitchCaseExpression() {
        if (cursor.matchKeyword(Keyword.SWITCH)) {
            return new ThenSwitchCaseExpression(parseExpression());
        }
        return new ThenSwitchCaseExpression(null);
    }

    private Expression parseElseSwitchCaseExpression() {
        if (cursor.matchKeyword(Keyword.SWITCH)) {
            return new ElseSwitchCaseExpression(parseExpression());
        }
        return new ElseSwitchCaseExpression(null);
    }

    private Expression parseRangeExpression(Expression nameExpression) {
        var item = cursor.expect(TokenType.IDENT, "range item name");
        cursor.expect(TokenType.COMMA, ",");
        var index = cursor.expect(TokenType.IDENT, "range index name");
        cursor.expectKeyword(Keyword.OF, "of");
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
        cursor.expect(TokenType.DOT, ".");
        var segments = new ArrayList<VariableExpression.Segment>();

        do {
            var ident = cursor.expect(TokenType.IDENT, "identifier after '.'");
            var args = parseMethodCallArguments();
            segments.add(args == null
                    ? new VariableExpression.Segment(ident.lexeme)
                    : new VariableExpression.Segment(ident.lexeme, args));
        } while (cursor.match(TokenType.DOT));

        return new VariableExpression(segments);
    }

    private List<Expression> parseMethodCallArguments() {
        if (!cursor.match(TokenType.LPAREN)) {
            return null;
        }
        var args = new ArrayList<Expression>();
        if (!cursor.check(TokenType.RPAREN)) {
            do {
                args.add(parseExpression());
            } while (cursor.match(TokenType.COMMA));
        }
        cursor.expect(TokenType.RPAREN, ")");
        return args;
    }

    /**
     * Parses either a function call or a standalone identifier expression.
     *
     * @return the parsed {@link FunctionCallExpression}
     * @throws TemplateParserException if the syntax is invalid
     */
    private FunctionCallExpression parseFunctionCallOrIdent() {
        var identToken = cursor.expect(TokenType.IDENT, "function or identifier");
        return parseSystemFunction(identToken);
    }

    /**
     * Parses a function call following a pipe operator ({@code |}).
     *
     * @return the parsed {@link FunctionCallExpression}
     * @throws TemplateParserException if the function name or arguments are invalid
     */
    private FunctionCallExpression parseFunctionCall() {
        var identToken = cursor.expect(TokenType.IDENT, "function after '|'");
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
        if (cursor.check(TokenType.COLON)) {
            cursor.advance();
            var functionName = cursor.expect(TokenType.IDENT, "function name required");
            var args = parseArguments();
            return new FunctionCallExpression(identToken.lexeme, functionName.lexeme, args);
        }
        var args = parseArguments();
        return new FunctionCallExpression(identToken.lexeme, args);
    }

    /**
     * Parses a list of function arguments until a stop token is encountered.
     *
     * @return a list of parsed argument {@link Expression}s, possibly empty
     */
    private List<Expression> parseArguments() {
        var args = new ArrayList<Expression>();
        var token = cursor.peek();
        if (token == null || isStopToken(token)) {
            return args;
        }
        do {
            if (isSpreadTokenSequence()) {
                cursor.advance();
                cursor.advance();
                cursor.advance();
                args.add(new SpreadExpression(parsePrimary()));
                continue;
            }
            args.add(parsePrimary());
        } while (cursor.match(TokenType.COMMA));
        return args;
    }

    private boolean isSpreadTokenSequence() {
        return cursor.check(TokenType.DOT)
                && cursor.checkNext(1, TokenType.DOT)
                && cursor.checkNext(2, TokenType.DOT);
    }

    /**
     * Determines whether the given token marks the end of an argument list or expression.
     *
     * @param token the token to check
     * @return {@code true} if the token is a stop token, {@code false} otherwise
     */
    private boolean isStopToken(Token token) {
        switch (token.type) {
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
}
