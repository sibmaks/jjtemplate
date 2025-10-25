package io.github.sibmaks.jjtemplate.lexer.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the set of reserved literal values recognized by the template lexer.
 * <p>
 * These include {@code true}, {@code false}, and {@code null}, which are treated
 * as special constant tokens during lexical analysis.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Reserved {
    TRUE("true"),
    FALSE("false"),
    NULL("null");

    /**
     * The reserved lexeme as it appears in the template source.
     */
    private final String lexem;

    /**
     * Compares the stored reserved lexeme with the provided string.
     *
     * @param condKey the string to compare against this reserved's lexeme
     * @return {@code true} if the given string equals this reserved's lexeme, {@code false} otherwise
     */
    public boolean eq(String condKey) {
        return lexem.equals(condKey);
    }
}
