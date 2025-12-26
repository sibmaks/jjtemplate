package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.cast;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Template function that converts a value to boolean.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class BooleanTemplateFunction implements TemplateFunction<Boolean> {

    private Boolean convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            var strValue = (String) value;
            if ("true".equals(strValue)) {
                return true;
            }
            if ("false".equals(strValue)) {
                return false;
            }
        }
        throw fail("cannot convert: " + value);
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return convert(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var arg = args.get(0);
        return convert(arg);
    }

    @Override
    public String getNamespace() {
        return "cast";
    }

    @Override
    public String getName() {
        return "boolean";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
