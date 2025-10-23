package io.github.sibmaks.jjtemplate.compiler.api;

import io.github.sibmaks.jjtemplate.compiler.TemplateCompilerImpl;

import java.util.Locale;

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
        return getInstance(Locale.getDefault());
    }

    /**
     * Get compiler instance with specific locale
     *
     * @param locale compiler locale
     * @return compiler instance
     */
    static TemplateCompiler getInstance(Locale locale) {
        return new TemplateCompilerImpl(locale);
    }
}
