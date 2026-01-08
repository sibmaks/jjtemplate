package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that returns the index of a substring within a string.
 * <p>
 * Evaluates the input string and the search value and returns the index of the
 * first occurrence, following {@link String#indexOf(String)} semantics.
 * </p>
 *
 * <p>
 * If the search value is not found, {@code -1} is returned.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringIndexOfTemplateFunction implements TemplateFunction<Integer> {

    private Integer indexOf(Object value, String str) {
        var string = String.valueOf(value);
        return string.indexOf(str);
    }

    @Override
    public Integer invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        if (pipeArg == null) {
            return null;
        }
        var value = String.valueOf(pipeArg);
        var str = String.valueOf(args.get(0));
        return indexOf(value, str);
    }

    @Override
    public Integer invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var arg0 = args.get(0);
        if (arg0 == null) {
            return null;
        }
        var value = String.valueOf(arg0);
        var str = String.valueOf(args.get(1));
        return indexOf(value, str);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "indexOf";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
