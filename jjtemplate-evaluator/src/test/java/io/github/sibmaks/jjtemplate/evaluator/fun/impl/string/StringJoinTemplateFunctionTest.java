package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringJoinTemplateFunctionTest {
    @InjectMocks
    private StringJoinTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("join", actual);
    }

    @Test
    void concatStrings() {
        var args = List.<Object>of(" ", "Hello", "World", "!");
        var actual = function.invoke(args);
        assertEquals("Hello World !", actual);
    }

    @Test
    void concatStringsWithPipeArg() {
        var args = List.<Object>of(" ", "Base", "-", "End");
        var actual = function.invoke(args, "X");
        assertEquals("Base - End X", actual);
    }

    @Test
    void concatWithPipeArgString() {
        var args = List.<Object>of(" ", "prefix", "middle");
        var actual = function.invoke(args, "suffix");
        assertEquals("prefix middle suffix", actual);
    }

    @Test
    void concatEmptyArgsThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:join: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void concatEmptyArgsWithPipeThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "test"));
        assertEquals("string:join: at least 1 argument required", exception.getMessage());
    }

}