package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class BooleanTemplateFunctionTest {
    @InjectMocks
    private BooleanTemplateFunction function;

    public static Stream<Arguments> justToBooleanCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(true, true),
                Arguments.of(false, false),
                Arguments.of("true", true),
                Arguments.of("false", false)
        );
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("boolean", actual);
    }

    @Test
    void nullPipeInvoke() {
        var actual = function.invoke(List.of(), null);
        assertNull(actual);
    }

    @Test
    void tooMuchArgsOnPipeInvoke() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, null));
        assertEquals("boolean: too much arguments passed", exception.getMessage());
    }

    @Test
    void tooMuchArgsOnInvoke() {
        var args = List.<Object>of(42, true);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("boolean: too much arguments passed", exception.getMessage());
    }

    @Test
    void noArgsOnInvoke() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("boolean: 1 argument required", exception.getMessage());
    }

    @Test
    void notBooleanString() {
        var args = List.<Object>of("text");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("boolean: cannot convert: text", exception.getMessage());
    }

    @Test
    void unknownType() {
        var args = List.<Object>of(42);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("boolean: cannot convert: 42", exception.getMessage());
    }

    @Test
    void nullArgInvoke() {
        var args = new ArrayList<>();
        args.add(null);
        var actual = function.invoke(args);
        assertNull(actual);
    }

    @ParameterizedTest
    @MethodSource("justToBooleanCases")
    void justToBooleanArg(Object input, Boolean excepted) {
        var args = new ArrayList<>();
        args.add(input);
        var actual = function.invoke(args);
        assertEquals(excepted, actual);
    }

    @ParameterizedTest
    @MethodSource("justToBooleanCases")
    void justToBooleanPipe(Object input, Boolean excepted) {
        var actual = function.invoke(List.of(), input);
        assertEquals(excepted, actual);
    }
}