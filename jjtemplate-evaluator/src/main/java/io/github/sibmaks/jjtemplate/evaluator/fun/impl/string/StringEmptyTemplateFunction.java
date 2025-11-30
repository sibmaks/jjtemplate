package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that checks whether a string is empty.
 *
 * <p>Returns {@code true} for {@code null} and empty {@link CharSequence} values.
 * Other types are not supported.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringEmptyTemplateFunction implements TemplateFunction<Boolean> {

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return isEmpty(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return isEmpty(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "empty";
    }
}
