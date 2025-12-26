package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringConcatTemplateFunctionTest {
    @InjectMocks
    private StringConcatTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("concat", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void concatStrings() {
        var args = List.<Object>of("Hello", " ", "World", "!");
        var actual = function.invoke(args);
        assertEquals("Hello World!", actual);
    }

    @Test
    void concatStringsWithPipeArg() {
        var args = List.<Object>of("Base", "-", "End");
        var actual = function.invoke(args, "X");
        assertEquals("Base-EndX", actual);
    }

    @Test
    void concatWithPipeArgString() {
        var args = List.<Object>of("prefix", "_", "middle");
        var actual = function.invoke(args, "_suffix");
        assertEquals("prefix_middle_suffix", actual);
    }

    @Test
    void concatEmptyArgsThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:concat: at least 1 argument required", exception.getMessage());
    }

    @Test
    void concatEmptyArgsWithPipeThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "test"));
        assertEquals("string:concat: at least 1 argument required", exception.getMessage());
    }

}
