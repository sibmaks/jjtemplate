package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public class StringReplaceTemplateFunction implements TemplateFunction<String> {

    private String replace(String value, String target, String replacement) {
        var string = String.valueOf(value);
        return string.replace(target, replacement);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        if (pipeArg == null) {
            return null;
        }
        var value = String.valueOf(pipeArg);
        var target = String.valueOf(args.get(0));
        var replacement = String.valueOf(args.get(1));
        return replace(value, target, replacement);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() != 3) {
            throw fail("3 arguments required");
        }
        var arg0 = args.get(0);
        if (arg0 == null) {
            return null;
        }
        var value = String.valueOf(arg0);
        var target = String.valueOf(args.get(1));
        var replacement = String.valueOf(args.get(2));
        return replace(value, target, replacement);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "replace";
    }
}
