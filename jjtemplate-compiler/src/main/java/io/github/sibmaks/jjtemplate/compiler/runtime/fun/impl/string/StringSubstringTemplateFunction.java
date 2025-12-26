package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that extracts a substring from a string.
 * <p>
 * Returns a portion of the source string defined by start and optional
 * end indices, following {@link String#substring(int)} and
 * {@link String#substring(int, int)} semantics.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringSubstringTemplateFunction implements TemplateFunction<String> {

    private String substr(Object value, int beginIndex, Integer endIndex) {
        var string = String.valueOf(value);
        var len = string.length();

        var b = beginIndex < 0 ? len + beginIndex : beginIndex;
        if (b < 0) {
            b = 0;
        }
        if (b > len) {
            b = len;
        }

        Integer e = null;
        if (endIndex != null) {
            int tmp = endIndex < 0 ? len + endIndex : endIndex;
            if (tmp < 0) {
                tmp = 0;
            }
            if (tmp > len) {
                tmp = len;
            }
            e = tmp;
        }

        if (e == null) {
            return string.substring(b);
        }

        if (e < b) {
            throw fail("endIndex < beginIndex after normalization");
        }

        return string.substring(b, e);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        if (args.size() > 2) {
            throw fail("too much arguments passed");
        }

        if (pipeArg == null) {
            return null;
        }

        var beginIndex = Integer.parseInt(String.valueOf(args.get(0)));
        var endIndex = args.size() == 2
                ? Integer.parseInt(String.valueOf(args.get(1)))
                : null;

        return substr(pipeArg, beginIndex, endIndex);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        if (args.size() > 3) {
            throw fail("too much arguments passed");
        }

        var value = args.get(0);
        if (value == null) {
            return null;
        }

        var beginIndex = Integer.parseInt(String.valueOf(args.get(1)));

        var endIndex = args.size() == 3
                ? Integer.parseInt(String.valueOf(args.get(2)))
                : null;

        return substr(value, beginIndex, endIndex);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "substr";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
