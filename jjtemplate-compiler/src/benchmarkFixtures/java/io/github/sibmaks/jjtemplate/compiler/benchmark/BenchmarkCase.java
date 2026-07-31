package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;

import java.util.Map;

/**
 * Immutable input and expected output for one benchmark scenario.
 */
public final class BenchmarkCase {
    private final TemplateScript script;
    private final Map<String, Object> context;
    private final Object expected;

    /**
     * Creates a benchmark case.
     *
     * @param script template to compile
     * @param context render context
     * @param expected expected render result
     */
    public BenchmarkCase(
            TemplateScript script,
            Map<String, Object> context,
            Object expected
    ) {
        this.script = script;
        this.context = context;
        this.expected = expected;
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
     * Returns the render context.
     *
     * @return render context
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Returns the expected render result.
     *
     * @return expected result
     */
    public Object getExpected() {
        return expected;
    }
}
