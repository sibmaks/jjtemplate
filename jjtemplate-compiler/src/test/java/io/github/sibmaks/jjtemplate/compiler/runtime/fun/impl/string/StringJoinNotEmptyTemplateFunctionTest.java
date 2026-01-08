package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class StringJoinNotEmptyTemplateFunctionTest {
    @InjectMocks
    private StringJoinNotEmptyTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("string", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("joinNotEmpty", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void concatStrings() {
        var args = new ArrayList<>();
        args.add(" ");
        args.add("Hello");
        args.add("");
        args.add(null);
        args.add("World");
        args.add('!');
        var actual = function.invoke(args);
        assertEquals("Hello World !", actual);
    }

    @Test
    void concatStringsWithPipeArg() {
        var args = new ArrayList<>();
        args.add(" ");
        args.add("Hello");
        args.add("");
        args.add(null);
        args.add("World");
        var actual = function.invoke(args, '!');
        assertEquals("Hello World !", actual);
    }

    @Test
    void concatWithPipeArgString() {
        var args = List.<Object>of(" ", "prefix", "", "middle");
        var actual = function.invoke(args, "suffix");
        assertEquals("prefix middle suffix", actual);
    }

    @Test
    void concatEmptyArgsThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("string:joinNotEmpty: at least 2 arguments required", exception.getMessage());
    }

    @Test
    void concatEmptyArgsWithPipeThrows() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "test"));
        assertEquals("string:joinNotEmpty: at least 1 argument required", exception.getMessage());
    }

}