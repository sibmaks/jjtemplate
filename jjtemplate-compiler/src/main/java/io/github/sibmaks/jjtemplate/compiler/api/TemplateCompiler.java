package io.github.sibmaks.jjtemplate.compiler.api;

import io.github.sibmaks.jjtemplate.compiler.TemplateCompilerImpl;

import java.util.Locale;

/**
 * Defines the API for compiling template scripts into executable templates.
 * <p>
 * Implementations of this interface transform parsed or raw template scripts
 * into {@link CompiledTemplate} instances that can be rendered later.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface TemplateCompiler {

    /**
     * Compiles the provided template script into a {@link CompiledTemplate}.
     *
     * @param script the source template script to compile
     * @return the compiled template ready for rendering
     */
    CompiledTemplate compile(TemplateScript script);

    /**
     * Returns the default {@link TemplateCompiler} instance
     * configured with the system default {@link Locale}.
     *
     * @return the default compiler instance
     */
    static TemplateCompiler getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a {@link TemplateCompiler} instance configured with the specified {@link Locale}.
     *
     * @param locale the locale to use for compiler configuration
     * @return a compiler instance for the given locale
     */
    static TemplateCompiler getInstance(Locale locale) {
        return new TemplateCompilerImpl(locale);
    }
}
