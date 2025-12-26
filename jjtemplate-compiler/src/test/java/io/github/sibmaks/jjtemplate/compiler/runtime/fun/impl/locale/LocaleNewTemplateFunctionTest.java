package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.locale;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class LocaleNewTemplateFunctionTest {
    @InjectMocks
    private LocaleNewTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        var actual = function.getNamespace();
        assertEquals("locale", actual);
    }

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("new", actual);
    }

    @Test
    void isStatic() {
        var actual = function.isDynamic();
        assertFalse(actual);
    }

    @Test
    void invokeNoArgsWithLocalePipe() {
        var pipe = Locale.FRANCE;
        var result = function.invoke(List.of(), pipe);
        assertEquals(pipe, result);
    }

    @Test
    void invokeNoArgsWithStringPipe() {
        var pipe = "en";
        var result = function.invoke(List.of(), pipe);
        assertEquals(new Locale("en"), result);
    }

    @Test
    void invokeOneArgWithPipe() {
        var args = List.<Object>of("en");
        var pipe = "US";
        var result = function.invoke(args, pipe);
        assertEquals(new Locale("en", "US"), result);
    }

    @Test
    void invokeTwoArgsWithPipe() {
        var args = List.<Object>of("en", "US");
        var pipe = "WIN";
        var result = function.invoke(args, pipe);
        assertEquals(new Locale("en", "US", "WIN"), result);
    }

    @Test
    void invokeTooManyArgsWithPipe() {
        var args = List.<Object>of("en", "US", "WIN");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args, "extra"));
        assertEquals("locale:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void invokeNoArgsShouldThrow() {
        var args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("locale:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void invokeSingleStringArg() {
        var args = List.<Object>of("en");
        var result = function.invoke(args);
        assertEquals(new Locale("en"), result);
    }

    @Test
    void invokeSingleLocaleArg() {
        var locale = Locale.GERMANY;
        var args = List.<Object>of(locale);
        var result = function.invoke(args);
        assertEquals(locale, result);
    }

    @Test
    void invokeTwoArgs() {
        var args = List.<Object>of("en", "US");
        var result = function.invoke(args);
        assertEquals(new Locale("en", "US"), result);
    }

    @Test
    void invokeThreeArgs() {
        var args = List.<Object>of("en", "US", "WIN");
        var result = function.invoke(args);
        assertEquals(new Locale("en", "US", "WIN"), result);
    }

    @Test
    void invokeTooManyArgs() {
        var args = List.<Object>of("en", "US", "WIN", "extra");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("locale:new: too much arguments passed", exception.getMessage());
    }
}