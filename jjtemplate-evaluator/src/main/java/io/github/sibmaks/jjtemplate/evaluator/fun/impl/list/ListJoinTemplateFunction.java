package io.github.sibmaks.jjtemplate.evaluator.fun.impl.list;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.4.1
 */
public final class ListJoinTemplateFunction implements TemplateFunction<String> {

    protected String join(String glue, List<Object> args) {
        var items = new ArrayList<String>();
        for (var arg : args) {
            if (arg == null) {
                items.add(null);
            } else if (arg instanceof Collection) {
                var collection = (Collection) arg;
                for (var item : collection) {
                    var value = String.valueOf(item);
                    items.add(value);
                }
            } else if (arg.getClass().isArray()) {
                var len = Array.getLength(arg);
                for (int i = 0; i < len; i++) {
                    var item = Array.get(arg, i);
                    var value = String.valueOf(item);
                    items.add(value);
                }
            } else {
                throw fail("unexcepted type: " + arg.getClass());
            }
        }
        return String.join(glue, items);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var glue = String.valueOf(args.get(0));
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        all.add(pipeArg);
        return join(glue, all);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var glue = String.valueOf(args.get(0));
        var all = args.stream()
                .skip(1)
                .collect(Collectors.toCollection(ArrayList::new));
        return join(glue, all);
    }

    @Override
    public String getNamespace() {
        return "list";
    }

    @Override
    public String getName() {
        return "join";
    }
}
