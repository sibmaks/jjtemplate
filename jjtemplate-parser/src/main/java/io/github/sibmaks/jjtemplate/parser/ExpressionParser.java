package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.lexer.api.TemplateLexerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses template expression strings into template parse contexts.
 * <p>
 * This parser uses the built-in lexer and parser to create a lightweight
 * template context that can be consumed by the compiler.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public class ExpressionParser {
    /**
     * Parses the given expression text into a template context.
     *
     * @param input expression string to parse
     * @return the root template context representing the expression
     * @throws TemplateParseException if the input contains syntax errors
     */
    public JJTemplateParser.TemplateContext parse(String input) {
        try {
            var lexer = new TemplateLexer(input);
            var tokens = lexer.tokens();
            var parts = new ArrayList<JJTemplateParser.TemplatePart>();

            int pos = 0;
            while (pos < tokens.size()) {
                var token = tokens.get(pos);
                if (token.type == TokenType.TEXT) {
                    parts.add(new JJTemplateParser.TextPart(token.lexeme));
                    pos++;
                    continue;
                }
                if (token.type == TokenType.OPEN_EXPR
                        || token.type == TokenType.OPEN_COND
                        || token.type == TokenType.OPEN_SPREAD) {
                    var type = mapInterpolationType(token.type);
                    var end = findClose(tokens, pos + 1);
                    var expressionTokens = tokens.subList(pos + 1, end);
                    var parser = new TemplateParser(expressionTokens);
                    var expression = parser.parseExpression();
                    parser.expectEnd();
                    parts.add(new JJTemplateParser.InterpolationPart(type, expression));
                    pos = end + 1;
                    continue;
                }
                throw new TemplateParseException("Unexpected token: " + token.type);
            }
            return new JJTemplateParser.TemplateContext(parts);
        } catch (TemplateLexerException | TemplateParserException e) {
            throw new TemplateParseException("Parse failed: '" + input + "'", e);
        }
    }

    private JJTemplateParser.InterpolationType mapInterpolationType(TokenType tokenType) {
        switch (tokenType) {
            case OPEN_COND:
                return JJTemplateParser.InterpolationType.CONDITION;
            case OPEN_SPREAD:
                return JJTemplateParser.InterpolationType.SPREAD;
            case OPEN_EXPR:
                return JJTemplateParser.InterpolationType.EXPRESSION;
            default:
                throw new TemplateParseException("Unknown interpolation type: " + tokenType);
        }
    }

    private int findClose(List<Token> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.CLOSE) {
                return i;
            }
        }
        throw new TemplateParseException("Missing closing '}}'");
    }
}
