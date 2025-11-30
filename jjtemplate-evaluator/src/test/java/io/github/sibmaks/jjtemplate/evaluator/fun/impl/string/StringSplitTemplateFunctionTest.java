package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

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
class StringSplitTemplateFunctionTest {
    @InjectMocks
    private StringSplitTemplateFunction function;

    static Stream<Arguments> splitCases() {
        return Stream.of(
                Arguments.of("a,b,c", ",", 0, List.of("a", "b", "c")),
                Arguments.of("a,b,c", ",", 2, List.of("a", "b,c")),
                Arguments.of("a--b--c", "--", 0, List.of("a", "b", "c")),
                Arguments.of("aaa", "a", 0, List.of()),
                Arguments.of("", ",", 0, List.of(""))
        );
    }

    static Stream<Arguments> splitCasesNoLimits() {
        return Stream.of(
                Arguments.of("a,b,c", ",", List.of("a", "b", "c")),
                Arguments.of("a--b--c", "--", List.of("a", "b", "c")),
                Arguments.of("aaa", "a", List.of()),
                Arguments.of("", ",", List.of(""))
        );
    }

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("split", actual);
    }

    @ParameterizedTest
    @MethodSource("splitCases")
    void splitDirect(String value, String regex, int limit, List<String> expected) {
        var args = List.<Object>of(value, regex, limit);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }


    @ParameterizedTest
    @MethodSource("splitCasesNoLimits")
    void splitDirectNoLimits(String value, String regex, List<String> expected) {
        var args = List.<Object>of(value, regex);
        var result = function.invoke(args);
        assertEquals(expected, result);
    }

    @Test
    void directInvokeMissingArguments() {
        var args = List.<Object>of("value");
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:split: at least 2 arguments required", ex.getMessage());
    }

    @Test
    void directInvokeTooManyArguments() {
        var args = List.<Object>of("a", ",", 1, 2);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:split: too much arguments passed", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("splitCases")
    void splitPipe(String value, String regex, int limit, List<String> expected) {
        var args = List.<Object>of(regex, limit);
        var actual = function.invoke(args, value);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("splitCasesNoLimits")
    void splitPipeNoLimits(String value, String regex, List<String> expected) {
        var args = List.<Object>of(regex);
        var actual = function.invoke(args, value);
        assertEquals(expected, actual);
    }

    @Test
    void pipeInvokeNoArgs() {
        var args = new ArrayList<>();
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "a,b"));
        assertEquals("string:split: at least 1 argument required", ex.getMessage());
    }

    @Test
    void pipeInvokeTooManyArgs() {
        var args = List.<Object>of(",", 1, 2);
        var ex = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "a,b"));
        assertEquals("string:split: too much arguments passed", ex.getMessage());
    }

    @Test
    void splitPipeNullValue() {
        var args = List.<Object>of(",");
        var result = function.invoke(args, null);
        assertEquals(List.of(), result);
    }

    @Test
    void splitDirectNullValue() {
        var args = new ArrayList<>();
        args.add(null);
        args.add(",");
        var result = function.invoke(args);
        assertEquals(List.of(), result);
    }
}