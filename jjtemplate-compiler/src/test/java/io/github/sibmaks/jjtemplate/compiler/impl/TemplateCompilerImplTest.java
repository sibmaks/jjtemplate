package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerImplTest {

    @Test
    void compileTemplateIsRequired() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        var exception = assertThrows(IllegalArgumentException.class, () -> compiler.compile(templateScript));
        assertEquals("'template' field required", exception.getMessage());
    }

}