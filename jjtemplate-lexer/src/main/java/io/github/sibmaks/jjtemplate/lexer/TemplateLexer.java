package io.github.sibmaks.jjtemplate.lexer;

import java.util.*;

/**
 * Lexical analyzer for the templating contract described by the user.
 * <p>
 * This lexer is designed to work over full template strings and emits two kinds of tokens:
 * - TEXT chunks (raw text outside of any {{ ... }} tag)
 * - EXPRESSION tokens (inside tags), including identifiers, numbers, strings, booleans, null, punctuation, pipes, etc.
 * <p>
 * Supported tag forms (recognized at lexing time):
 * {{ <expr> }}      -> OPEN_EXPR / CLOSE
 * {{? <expr> }}     -> OPEN_COND / CLOSE    (conditional array insertion)
 * {{. <expr> }}     -> OPEN_SPREAD / CLOSE  (spread into parent array/object)
 * <p>
 * Notes
 * -----
 * - Strings use single quotes per the examples. Escapes supported: \\' \\" \\n * - Numbers: integers and floating-point (e.g., 42, 3.1415, -7, +2.5, 1e10, -3.2E-4)
 * - Keywords (case-insensitive where it matters): case, then, else, range, of.
 * - Functions and logical operators are tokenized as IDENTs (e.g., str, int, double, boolean, len, empty,
 * upper, lower, not, eq, neq, lt, le, gt, ge, and, or, list, concat, optional, default). Parser can
 * treat certain idents as keywords if desired.
 * - Variable access: a leading DOT followed by segments (e.g., .a, .parent.child). Lexer emits DOT and IDENT
 * tokens separately; the parser can assemble the chain.
 * - Whitespace inside {{ ... }} is skipped between tokens.
 * - Outside of tags, everything is emitted as TEXT (can be empty between adjacent tags).
 */
public final class TemplateLexer {

    // ==== Public API ========================================================

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "case", "then", "else", "range", "of"
    ));
    private final String input;
    private final int n;
    private int pos = 0;
    private Mode mode = Mode.TEXT;
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

    // ==== Core lexing =======================================================

    /**
     * Produce all tokens for the input. The stream alternates between TEXT and expression tokens depending on tags.
     */
    public List<Token> tokens() {
        List<Token> out = new ArrayList<>();
        while (pos < n) {
            if (mode == Mode.TEXT) {
                out.add(lexText());
            } else {
                Token t = lexExprToken();
                out.add(t);
            }
        }
        return out;
    }

    private Token lexText() {
        int start = pos;
        while (pos < n) {
            if (peek() == '{' && peek2() == '{') {
                // Emit accumulated TEXT (possibly empty), then switch to EXPR and emit tag opener token.
                String chunk = input.substring(start, pos);
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
            } else {
                pos++;
            }
        }
        // End reached, emit remaining TEXT
        String chunk = input.substring(start, pos);
        return new Token(TokenType.TEXT, chunk, start, pos);
    }

    // ---- Helpers -----------------------------------------------------------

    private Token lexExprToken() {
        skipWhitespace();
        int start = pos;
        if (pos >= n) {
            // Unterminated expression; return empty TEXT to finish gracefully
            mode = Mode.TEXT;
            return new Token(TokenType.TEXT, "", pos, pos);
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
            case ':':
                pos++;
                return new Token(TokenType.COLON, ":", start, pos);
            case '(':
                pos++;
                return new Token(TokenType.LPAREN, "(", start, pos);
            case ')':
                pos++;
                return new Token(TokenType.RPAREN, ")", start, pos);
            case '{':
                pos++;
                return new Token(TokenType.LBRACE, "{", start, pos);
            case '}':
                pos++;
                return new Token(TokenType.RBRACE, "}", start, pos);
            case '[':
                pos++;
                return new Token(TokenType.LBRACKET, "[", start, pos);
            case ']':
                pos++;
                return new Token(TokenType.RBRACKET, "]", start, pos);
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
                pos++;
                return new Token(TokenType.TEXT, String.valueOf(c), start, pos);
        }
    }

    private Token lexString() {
        int start = pos;
        pos++; // opening '
        StringBuilder sb = new StringBuilder();
        boolean closed = false;
        while (pos < n) {
            char c = input.charAt(pos++);
            if (c == '\'') {
                closed = true;
                break;
            }
            if (c == '\\' && pos < n) {
                char e = input.charAt(pos++);
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
        int end = pos;
        // Even if not closed, we emit STRING with what we have
        return new Token(TokenType.STRING, sb.toString(), start, end);
    }

    private Token lexNumber() {
        int start = pos;
        if (peek() == '+' || peek() == '-') pos++;
        boolean hasDot = false;
        while (pos < n) {
            char c = peek();
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
            int save = pos;
            pos++;
            if (pos < n && (peek() == '+' || peek() == '-')) pos++;
            if (pos < n && isDigit(peek())) {
                while (pos < n && isDigit(peek())) pos++;
            } else {
                // not a valid exponent, roll back to save
                pos = save;
            }
        }
        String num = input.substring(start, pos);
        return new Token(TokenType.NUMBER, num, start, pos);
    }

    private Token lexWord() {
        int start = pos;
        pos++; // consume first
        while (pos < n && isIdentPart(peek())) pos++;
        String word = input.substring(start, pos);
        String lower = word.toLowerCase(Locale.ROOT);
        if ("true".equals(lower) || "false".equals(lower)) {
            return new Token(TokenType.BOOLEAN, lower, start, pos);
        }
        if ("null".equals(lower)) {
            return new Token(TokenType.NULL, lower, start, pos);
        }
        if (KEYWORDS.contains(lower)) {
            return new Token(TokenType.KEYWORD, lower, start, pos);
        }
        return new Token(TokenType.IDENT, word, start, pos);
    }

    private void skipWhitespace() {
        while (pos < n) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else break;
        }
    }

    private char peek() {
        return pos < n ? input.charAt(pos) : '\0';
    }

    private char peek2() {
        return (pos + 1) < n ? input.charAt(pos + 1) : '\0';
    }

    public enum TokenType {
        // Outside expressions
        TEXT,

        // Tag delimiters
        OPEN_EXPR,      // "{{"
        OPEN_COND,      // "{{?"
        OPEN_SPREAD,    // "{{."
        CLOSE,          // "}}"

        // Punctuation / Operators (inside expressions)
        PIPE,           // '|'
        DOT,            // '.'
        COMMA,          // ','
        COLON,          // ':'
        LPAREN,         // '('
        RPAREN,         // ')'
        LBRACE,         // '{'
        RBRACE,         // '}'
        LBRACKET,       // '['
        RBRACKET,       // ']'

        // Literals
        STRING,         // 'text' with escapes
        NUMBER,         // 42, 3.14, 1e-3
        BOOLEAN,        // true | false
        NULL,           // null

        // Words
        IDENT,          // functions, variable names, etc.
        KEYWORD,        // case, then, else, range, of
    }

    /**
     * Mode indicates whether we are lexing raw TEXT or an expression within a tag.
     */
    private enum Mode {TEXT, EXPR}

    public static final class Token {
        public final TokenType type;
        public final String lexeme; // For TEXT, STRING (without quotes), IDENT/KEYWORD, BOOLEAN/NULL, or raw chunk
        public final int start;     // start index (inclusive) in original input
        public final int end;       // end index (exclusive)

        Token(TokenType type, String lexeme, int start, int end) {
            this.type = type;
            this.lexeme = lexeme;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return type + (lexeme != null ? ("(" + lexeme + ")") : "") + "@" + start + ".." + end;
        }
    }

}
