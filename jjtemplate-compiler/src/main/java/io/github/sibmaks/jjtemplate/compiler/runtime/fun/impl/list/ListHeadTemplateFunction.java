package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.list;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public final class ListHeadTemplateFunction implements TemplateFunction<Object> {

    private Object getHead(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            var collection = ((Collection<?>) value);
            var iterator = collection.iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            return iterator.next();
        }
        if (value.getClass().isArray()) {
            var len = Array.getLength(value);
            if (len == 0) {
                return null;
            }
            return Array.get(value, 0);
        }
        throw fail("unsupported type: " + value.getClass());
    }

    @Override
    public Object invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return getHead(pipeArg);
    }

    @Override
    public Object invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if (args.size() != 1) {
            throw fail("too much arguments passed");
        }
        return getHead(args.get(0));
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "head";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
