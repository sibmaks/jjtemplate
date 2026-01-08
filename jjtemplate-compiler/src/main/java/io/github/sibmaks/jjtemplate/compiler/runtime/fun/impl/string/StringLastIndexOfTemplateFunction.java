package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that returns the index of the last occurrence of a substring
 * within a string.
 * <p>
 * Evaluates the input string and the search value and returns the index of the
 * last occurrence, following {@link String#lastIndexOf(String)} semantics.
 * </p>
 *
 * <p>
 * If the search value is not found, {@code -1} is returned.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringLastIndexOfTemplateFunction implements TemplateFunction<Integer> {

    private Integer lastIndexOf(Object value, String str) {
        var string = String.valueOf(value);
        return string.lastIndexOf(str);
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
        return lastIndexOf(value, str);
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
        return lastIndexOf(value, str);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "lastIndexOf";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
