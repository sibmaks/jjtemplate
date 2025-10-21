package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.Token;
import io.github.sibmaks.jjtemplate.lexer.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.*;

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
 */
public final class TemplateParser {

    private final List<Token> tokens;
    private int pos = 0;

    public TemplateParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parseExpression() {
        var expr = parsePrimary();
        if (match(TokenType.PIPE)) {
            var chain = new ArrayList<FunctionCallExpression>();
            do {
                chain.add(parseFunctionCall());
            } while (match(TokenType.PIPE));
            expr = new PipeExpression(expr, chain);
        }
        return expr;
    }

    public Expression parseTemplate() {
        var parts = new ArrayList<Expression>();

        while (pos < tokens.size()) {
            var t = peek();

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
                    return new LiteralExpression(Double.parseDouble(num));
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

    private VariableExpression parseVariable() {
        expect(TokenType.DOT, ".");
        var path = new ArrayList<String>();
        do {
            path.add(expect(TokenType.IDENT, "identifier after '.'").lexeme);
        } while (match(TokenType.DOT));
        return new VariableExpression(path);
    }

    private FunctionCallExpression parseFunctionCallOrIdent() {
        var nameTok = expect(TokenType.IDENT, "function or identifier");
        var name = nameTok.lexeme;
        var args = parseArguments();
        return new FunctionCallExpression(name, args);
    }

    private FunctionCallExpression parseFunctionCall() {
        var nameTok = expect(TokenType.IDENT, "function after '|'");
        var name = nameTok.lexeme;
        var args = parseArguments();
        return new FunctionCallExpression(name, args);
    }

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

    private boolean isStopToken(Token t) {
        switch (t.type) {
            case PIPE:
            case CLOSE:
            case COMMA:
            case RPAREN:
            case RBRACKET:
            case RBRACE:
                return true;
            default:
                return false;
        }
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        var t = peek();
        return t != null && t.type == type;
    }

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

    private TemplateParserException error(String msg) {
        return new TemplateParserException(msg, pos);
    }

}
