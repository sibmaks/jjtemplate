package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class DefaultTemplateFunctionTest {
    @InjectMocks
    private DefaultTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("default", actual);
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("default: 2 arguments required", exception.getMessage());
    }

    @Test
    void noArgsOnPipeInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("default: 1 argument required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("checkCases")
    void checkWhenOneArg(Object arg, Object pipe, Object expected) {
        var args = new ArrayList<>();
        args.add(arg);
        var actual = function.invoke(args, pipe);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("checkCases")
    void checkWhenTwoArgs(Object arg, Object pipe, Object expected) {
        var args = new ArrayList<>();
        args.add(pipe);
        args.add(arg);
        var actual = function.invoke(args);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> checkCases() {
        return Stream.of(
                Arguments.of("fail", "ok", "ok"),
                Arguments.of("ok", null, "ok"),
                Arguments.of(null, "ok", "ok")
        );
    }

}