package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that returns the length of a string.
 *
 * <p>Returns {@code 0} for {@code null}. Only {@link CharSequence} values
 * are supported; other types result in an error.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringLengthTemplateFunction implements TemplateFunction<Integer> {

    private int getLength(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public Integer invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return getLength(pipeArg);
    }

    @Override
    public Integer invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return getLength(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "len";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
