package io.github.sibmaks.jjtemplate.compiler.api;

import io.github.sibmaks.jjtemplate.compiler.TemplateCompilerImpl;

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface TemplateCompiler {

    /**
     * Compile template script
     *
     * @param script source template script
     * @return compiled template
     */
    CompiledTemplate compile(TemplateScript script);

    /**
     * Get default compiler instance
     *
     * @return compiler instance
     */
    static TemplateCompiler getInstance() {
        return new TemplateCompilerImpl();
    }
}
