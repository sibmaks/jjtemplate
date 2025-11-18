package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public class StringTrimTemplateFunction implements TemplateFunction<String> {

    private String trim(Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.trim();
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if(!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return trim(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("1 argument required");
        }
        if(args.size() != 1) {
            throw fail("too much arguments passed");
        }
        var arg = args.get(0);
        return trim(arg);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "trim";
    }
}
