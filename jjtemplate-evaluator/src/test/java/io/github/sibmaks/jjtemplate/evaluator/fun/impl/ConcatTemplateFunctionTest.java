package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class ConcatTemplateFunctionTest {
    @InjectMocks
    private ConcatTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("concat", actual);
    }

    @Test
    void withoutArguments() {
        var args = List.<ExpressionValue>of();
        var pipe = ExpressionValue.empty();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("concat: at least 1 argument required", exception.getMessage());
    }

    @Test
    void concatToList() {
        var arg = List.of(42);
        var arrayArg = new String[]{UUID.randomUUID().toString()};
        var listArg = List.of(UUID.randomUUID().toString());
        var pipeArg = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(arrayArg),
                ExpressionValue.of(listArg),
                ExpressionValue.of(null)
        );
        var pipe = ExpressionValue.of(pipeArg);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var excepted = Stream.of(42, arrayArg[0], listArg.get(0), null, pipeArg).collect(Collectors.toList());
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void concatIntArrayToList() {
        var arg = new int[]{42};
        var arrayArg = new String[]{UUID.randomUUID().toString()};
        var listArg = List.of(UUID.randomUUID().toString());
        var pipeArg = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(arrayArg),
                ExpressionValue.of(listArg),
                ExpressionValue.of(null)
        );
        var pipe = ExpressionValue.of(pipeArg);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var excepted = Stream.of(42, arrayArg[0], listArg.get(0), null, pipeArg).collect(Collectors.toList());
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void concatObjectArrayToList() {
        var arg = new Integer[]{42};
        var arrayArg = new String[]{UUID.randomUUID().toString()};
        var listArg = List.of(UUID.randomUUID().toString());
        var pipeArg = UUID.randomUUID().toString();
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.of(arrayArg),
                ExpressionValue.of(listArg),
                ExpressionValue.of(null),
                ExpressionValue.empty()
        );
        var pipe = ExpressionValue.of(pipeArg);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        var excepted = Stream.of(42, arrayArg[0], listArg.get(0), null, pipeArg).collect(Collectors.toList());
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void concatToStringNoPipe() {
        var arg = UUID.randomUUID().toString();
        var intArg = UUID.randomUUID().hashCode();
        var args = List.of(
                ExpressionValue.of(arg),
                ExpressionValue.empty(),
                ExpressionValue.of(intArg)
        );
        var pipe = ExpressionValue.empty();
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(arg + intArg, actual.getValue());
    }

    @Test
    void concatToStringWithPipe() {
        var arg = UUID.randomUUID().toString();
        var intArg = UUID.randomUUID().hashCode();
        var pipeArg = UUID.randomUUID().hashCode();
        var args = List.of(
                ExpressionValue.empty(),
                ExpressionValue.of(arg),
                ExpressionValue.of(intArg)
        );
        var pipe = ExpressionValue.of(pipeArg);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(arg + intArg + pipeArg, actual.getValue());
    }

    @Test
    void concatToStringWhen1stIsNull() {
        var arg = UUID.randomUUID().toString();
        var intArg = UUID.randomUUID().hashCode();
        var pipeArg = UUID.randomUUID().hashCode();
        var args = List.of(
                ExpressionValue.of(null),
                ExpressionValue.of(arg),
                ExpressionValue.of(intArg)
        );
        var pipe = ExpressionValue.of(pipeArg);
        var actual = function.invoke(args, pipe);
        assertFalse(actual.isEmpty());
        assertEquals(null + arg + intArg + pipeArg, actual.getValue());
    }

}