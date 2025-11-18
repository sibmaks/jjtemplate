package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public class StringSubstringTemplateFunction implements TemplateFunction<String> {

    private String substr(Object value, int beginIndex, Integer endIndex) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);

        if (beginIndex < 0 || beginIndex > string.length()) {
            throw fail("beginIndex out of bounds");
        }

        if (endIndex == null) {
            return string.substring(beginIndex);
        }

        if (endIndex < beginIndex || endIndex > string.length()) {
            throw fail("endIndex out of bounds");
        }

        return string.substring(beginIndex, endIndex);
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
}
