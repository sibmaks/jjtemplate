package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;

import java.util.Map;

/**
 * Template, runtime data and compile-time types for a type-binding benchmark.
 */
public final class TypeBindingCase {
    private final TemplateScript script;
    private final Map<String, Object> context;
    private final TemplateCompileContext compileContext;
    private final Object expected;
    private final boolean boundPathExpected;

    /**
     * Creates a type-binding case.
     *
     * @param script template to compile
     * @param context render context
     * @param compileContext compile-time type information
     * @param expected expected render result
     * @param boundPathExpected whether type binding should resolve a bound chain
     */
    public TypeBindingCase(
            TemplateScript script,
            Map<String, Object> context,
            TemplateCompileContext compileContext,
            Object expected,
            boolean boundPathExpected
    ) {
        this.script = script;
        this.context = context;
        this.compileContext = compileContext;
        this.expected = expected;
        this.boundPathExpected = boundPathExpected;
    }

    /**
     * Returns the template script.
     *
     * @return template script
     */
    public TemplateScript getScript() {
        return script;
    }

    /**
     * Returns runtime data.
     *
     * @return runtime context
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Returns compile-time type information.
     *
     * @return compile context
     */
    public TemplateCompileContext getCompileContext() {
        return compileContext;
    }

    /**
     * Returns the expected render result.
     *
     * @return expected result
     */
    public Object getExpected() {
        return expected;
    }

    /**
     * Tells whether the typed compiler should produce a bound chain.
     *
     * @return true for a bound DTO path
     */
    public boolean isBoundPathExpected() {
        return boundPathExpected;
    }

    /**
     * Compiles this case using the selected source of root variable types.
     *
     * @param compiler compiler instance
     * @param binding binding mode
     * @return compiled template
     */
    public CompiledTemplate compile(
            TemplateCompiler compiler,
            TypeBindingMode binding
    ) {
        if (binding == TypeBindingMode.EXPLICIT_CONTEXT) {
            return compiler.compile(script, compileContext);
        }
        return compiler.compile(script);
    }
}
