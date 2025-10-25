package io.github.sibmaks.jjtemplate.lexer.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the set of reserved keywords recognized by the template lexer.
 * <p>
 * Each keyword is associated with its string lexeme as it appears in the template source.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Keyword {
    CASE("case"),
    THEN("then"),
    ELSE("else"),
    RANGE("range"),
    OF("of");

    /**
     * The keyword lexeme as it appears in the template source.
     */
    private final String lexem;

    /**
     * Compares the stored keyword lexeme with the provided string.
     *
     * @param condKey the string to compare against this keyword's lexeme
     * @return {@code true} if the given string equals this keyword's lexeme, {@code false} otherwise
     */
    public boolean eq(String condKey) {
        return lexem.equals(condKey);
    }

    /**
     * Searches for a {@link Keyword} constant matching the specified word.
     *
     * @param word the word to look up
     * @return the matching {@link Keyword} if found, or {@code null} if the word is not a keyword
     */
    public static Keyword find(String word) {
        for (var keyword : values()) {
            if (keyword.lexem.equals(word)) {
                return keyword;
            }
        }
        return null;
    }

}
