package io.github.sibmaks.jjtemplate.lexer;

import io.github.sibmaks.jjtemplate.lexer.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lexical analyzer for the templating contract described by the user.
 * <p>
 * This lexer is designed to work over full template strings and emits two kinds of tokens:
 * <ul>
 * <li>TEXT chunks (raw text outside of any {@code {{ ... }} } tag)</li>
 * <li>EXPRESSION tokens (inside tags), including identifiers, numbers, strings, booleans, null, punctuation, pipes, etc.</li>
 * </ul>
 * <p>
 * Supported tag forms (recognized at lexing time):
 * <ul>
 * <li>{@code {{ <expr> }}}      -&gt; OPEN_EXPR / CLOSE</li>
 * <li>{@code {{? <expr> }}}     -&gt; OPEN_COND / CLOSE    (conditional array insertion)</li>
 * <li>{@code {{. <expr> }}}     -&gt; OPEN_SPREAD / CLOSE  (spread into parent array/object)</li>
 * </ul>
 * <p>
 * Notes
 * -----
 * <ul>
 * <li>Strings use single quotes per the examples. Escapes supported: \\' \\" \\n</li>
 * <li>Numbers: integers and floating-point (e.g., 42, 3.1415, -7, +2.5, 1e10, -3.2E-4)</li>
 * <li>Keywords (case-insensitive where it matters): switch, then, else, range, of.</li>
 * <li>Functions and logical operators are tokenized as IDENTs (e.g., str, int, float, boolean, len, empty,
 * upper, lower, not, eq, neq, lt, le, gt, ge, and, or, list, concat, default). Parser can
 * treat certain idents as keywords if desired.</li>
 * <li>Variable access: a leading DOT followed by segments (e.g., .a, .parent.child). Lexer emits DOT and IDENT
 * tokens separately; the parser can assemble the chain.</li>
 * <li>Whitespace inside {{ ... }} is skipped between tokens.</li>
 * <li>Outside of tags, everything is emitted as TEXT (can be empty between adjacent tags).</li>
 * </ul>
 */
public final class TemplateLexer {
    private final String input;
    private final int n;
    private int pos = 0;
    private Mode mode = Mode.TEXT;

    /**
     * Creates a new {@code TemplateLexer} instance for the given input.
     *
     * @param input the full template source text to be tokenized
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public TemplateLexer(String input) {
        this.input = Objects.requireNonNull(input, "input");
        this.n = input.length();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    private static boolean isIdentPart(char c) {
        return isIdentStart(c) || isDigit(c);
    }

    /**
     * Returns a list of all {@link Token}s produced from the input.
     * <p>
     * The lexer alternates between {@code TEXT} and {@code EXPR} modes depending
     * on whether it is inside or outside of {@code {{ ... }}} tags.
     * </p>
     *
     * @return an ordered list of tokens representing the parsed template
     * @throws TemplateLexerException if an invalid or unterminated construct is encountered
     */
    public List<Token> tokens() {
        var out = new ArrayList<Token>();
        try {
            while (pos < n) {
                if (mode == Mode.TEXT) out.add(lexText());
                else out.add(lexExprToken());
            }
        } catch (TemplateLexerException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateLexerException(input, "Unexpected error: " + e.getMessage(), pos);
        }
        if (mode == Mode.EXPR) {
            throw new TemplateLexerException(input, "Unterminated template: missing closing '}}'", pos);
        }
        return out;
    }

    private Token lexText() {
        int start = pos;
        while (pos < n) {
            if (peek() == '{' && peek2() == '{') {
                // Emit accumulated TEXT (possibly empty), then switch to EXPR and emit tag opener token.
                var chunk = input.substring(start, pos);
                if (!chunk.isEmpty()) {
                    return new Token(TokenType.TEXT, chunk, start, pos);
                }
                // chunk is empty -> we are exactly at a tag
                // Determine which kind of opener
                int openerStart = pos;
                pos += 2; // consume "{{"
                char c = peek();
                if (c == '?') {
                    pos++;
                    mode = Mode.EXPR;
                    return new Token(TokenType.OPEN_COND, "{{?", openerStart, pos);
                }
                if (c == '.') {
                    pos++;
                    mode = Mode.EXPR;
                    return new Token(TokenType.OPEN_SPREAD, "{{.", openerStart, pos);
                }
                mode = Mode.EXPR;
                return new Token(TokenType.OPEN_EXPR, "{{", openerStart, pos);
            } else pos++;
        }
        var chunk = input.substring(start, pos);
        return new Token(TokenType.TEXT, chunk, start, pos);
    }

    private Token lexExprToken() {
        skipWhitespace();
        var start = pos;
        if (pos >= n) {
            throw new TemplateLexerException(input, "Unexpected end inside expression", pos);
        }

        // Close delimiter
        if (peek() == '}' && peek2() == '}') {
            pos += 2;
            mode = Mode.TEXT;
            return new Token(TokenType.CLOSE, "}}", start, pos);
        }

        char c = peek();
        switch (c) {
            case '|':
                pos++;
                return new Token(TokenType.PIPE, "|", start, pos);
            case '.':
                pos++;
                return new Token(TokenType.DOT, ".", start, pos);
            case ',':
                pos++;
                return new Token(TokenType.COMMA, ",", start, pos);
            case '?':
                pos++;
                return new Token(TokenType.QUESTION, "?", start, pos);
            case ':':
                pos++;
                return new Token(TokenType.COLON, ":", start, pos);
            case '(':
                pos++;
                return new Token(TokenType.LPAREN, "(", start, pos);
            case ')':
                pos++;
                return new Token(TokenType.RPAREN, ")", start, pos);
            case '\'':
                return lexString();
            default:
                if (isDigit(c) || (c == '-' || c == '+') && (isDigit(peek2()) || peek2() == '.')) {
                    return lexNumber();
                }
                if (isIdentStart(c)) {
                    return lexWord();
                }
                // Unknown character inside expr
                throw new TemplateLexerException(input, "Unexpected character '" + c + "'", pos);
        }
    }

    private Token lexString() {
        var start = pos;
        pos++; // opening '
        var sb = new StringBuilder();
        var closed = false;
        while (pos < n) {
            var c = input.charAt(pos++);
            if (c == '\'') {
                closed = true;
                break;
            }
            if (c == '\\' && pos < n) {
                var e = input.charAt(pos++);
                switch (e) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    default:
                        sb.append(e);
                        break; // unknown escape -> passthrough
                }
            } else {
                sb.append(c);
            }
        }
        if (!closed) {
            throw new TemplateLexerException(input, "Unterminated string literal", pos);
        }
        // Even if not closed, we emit STRING with what we have
        return new Token(TokenType.STRING, sb.toString(), start, pos);
    }

    private Token lexNumber() {
        var start = pos;
        if (peek() == '+' || peek() == '-') {
            pos++;
        }
        var hasDot = false;
        while (pos < n) {
            var c = peek();
            if (isDigit(c)) {
                pos++;
                continue;
            }
            if (c == '.' && !hasDot) {
                hasDot = true;
                pos++;
                continue;
            }
            break;
        }
        // exponent
        if (pos < n && (peek() == 'e' || peek() == 'E')) {
            var save = pos;
            pos++;
            if (pos < n && (peek() == '+' || peek() == '-')) {
                pos++;
            }
            if (pos < n && isDigit(peek())) {
                while (pos < n && isDigit(peek())) {
                    pos++;
                }
            } else {
                // not a valid exponent, roll back to save
                pos = save;
            }
        }
        var num = input.substring(start, pos);
        return new Token(TokenType.NUMBER, num, start, pos);
    }

    private Token lexWord() {
        var start = pos;
        do pos++; // consume first
        while (pos < n && isIdentPart(peek()));
        var word = input.substring(start, pos);
        if (Reserved.TRUE.eq(word)) {
            return new Token(TokenType.BOOLEAN, Reserved.TRUE.getLexem(), start, pos);
        }
        if (Reserved.FALSE.eq(word)) {
            return new Token(TokenType.BOOLEAN, Reserved.FALSE.getLexem(), start, pos);
        }
        if (Reserved.NULL.eq(word)) {
            return new Token(TokenType.NULL, Reserved.NULL.getLexem(), start, pos);
        }
        var keyword = Keyword.find(word);
        if (keyword != null) {
            return new Token(TokenType.KEYWORD, keyword.getLexem(), start, pos);
        }
        return new Token(TokenType.IDENT, word, start, pos);
    }

    private void skipWhitespace() {
        while (pos < n && Character.isWhitespace(peek())) {
            pos++;
        }
    }

    private char peek() {
        return pos < n ? input.charAt(pos) : '\0';
    }

    private char peek2() {
        return (pos + 1 < n) ? input.charAt(pos + 1) : '\0';
    }

    /**
     * Internal lexer mode indicating whether parsing occurs in raw text ({@code TEXT})
     * or inside a template expression ({@code EXPR}).
     */
    private enum Mode {TEXT, EXPR}

}
