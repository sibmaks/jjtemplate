package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that trims leading and trailing whitespace from a string.
 * <p>
 * Applies semantics equivalent to {@link String#trim()} to the evaluated
 * input value.
 * </p>
 *
 * <p>
 * The function operates on string representations of values and does not
 * modify the evaluation context.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringTrimTemplateFunction implements TemplateFunction<String> {

    private String trim(Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.trim();
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return trim(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var arg = args.get(0);
        return trim(arg);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "trim";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
