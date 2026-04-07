package io.github.sibmaks.jjtemplate.lexer.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeywordTest {

    @Test
    void eqShouldCompareLexeme() {
        assertTrue(Keyword.SWITCH.eq("switch"));
        assertFalse(Keyword.SWITCH.eq("range"));
    }

    @Test
    void findShouldReturnNullForUnknownWord() {
        assertNull(Keyword.find("missing"));
    }
}
