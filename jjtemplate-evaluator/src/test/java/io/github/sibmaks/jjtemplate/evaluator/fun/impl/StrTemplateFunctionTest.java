package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StrTemplateFunctionTest {
    @InjectMocks
    private StrTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("str", actual);
    }

    @Test
    void emptyInputRetuned() {
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(List.of(), pipe);
        assertEquals(pipe, actual);
    }

    @Test
    void onNullEmptyReturned() {
        var pipe = ExpressionValue.of(null);
        var actual = function.invoke(List.of(), pipe);
        assertEquals(pipe, actual);
    }

    @ParameterizedTest
    @MethodSource("justToStringCases")
    void justToString(Object input, String excepted) {
        var pipe = ExpressionValue.of(input);
        var actual = function.invoke(List.of(), pipe);
        assertFalse(actual.isEmpty());
        assertEquals(excepted, actual.getValue());
    }

    public static Stream<Arguments> justToStringCases() {
        return Stream.of(
                Arguments.of(42, "42"),
                Arguments.of(42.31, "42.31"),
                Arguments.of(true, "true"),
                Arguments.of(false, "false"),
                Arguments.of("text", "text")
        );
    }
}