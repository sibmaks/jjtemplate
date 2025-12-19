package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.frontend.exception.TemplateParseException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerImplTest {

    @Test
    void compileTemplateIsRequired() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("'template' field required", exception.getMessage());
    }

    @Test
    void compileTemplateWhenTemplateIsInvalid() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        templateScript.setTemplate("{{");
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("Error compiling template", exception.getMessage());
        var cause = assertInstanceOf(TemplateParseException.class, exception.getCause());
        assertEquals("Parse string: '{{' failed", cause.getMessage());
    }

    @Test
    void compileTemplateWhenDefinitionIsInvalid() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        var definition = new Definition();
        definition.put("key", "{{");
        templateScript.setDefinitions(List.of(definition));
        templateScript.setTemplate("ok");
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("Error compiling definition", exception.getMessage());
        var cause = assertInstanceOf(TemplateParseException.class, exception.getCause());
        assertEquals("Parse string: '{{' failed", cause.getMessage());
    }

}