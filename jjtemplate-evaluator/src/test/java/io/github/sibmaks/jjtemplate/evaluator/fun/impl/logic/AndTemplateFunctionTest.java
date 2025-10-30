package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class AndTemplateFunctionTest {
    @InjectMocks
    private AndTemplateFunction function;

    public static Stream<Arguments> cmpCases() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, false),
                Arguments.of(false, true, false),
                Arguments.of(false, false, false)
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("and", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("and: 2 arguments required", exception.getMessage());
    }

    @Test
    void noArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("and: 1 argument required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passAsArguments(boolean left, boolean right, boolean expected) {
        var actual = function.invoke(List.of(left, right));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("cmpCases")
    void passWithPipe(boolean left, boolean right, boolean expected) {
        var actual = function.invoke(List.of(left), right);
        assertEquals(expected, actual);
    }

    @Test
    void passInvalidLeftArgument() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("and: All arguments must be a boolean", exception.getMessage());
    }

    @Test
    void passInvalidRightArgument() {
        var args = List.<Object>of(true, 42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("and: All arguments must be a boolean", exception.getMessage());
    }

}