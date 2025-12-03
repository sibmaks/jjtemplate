package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Template function that formats a string using {@link String#format}.
 *
 * <p>Supports optional {@link Locale} as the first argument. Remaining
 * arguments are passed to the formatting pattern. In pipe form, the piped
 * value is appended to the argument list.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class StringFormatTemplateFunction implements TemplateFunction<String> {

    private String format(List<Object> args) {
        var locale = Locale.getDefault();
        String format;
        var firstArg = args.remove(0);
        if (firstArg instanceof Locale) {
            locale = (Locale) firstArg;
            if (args.isEmpty()) {
                throw fail("at least 2 arguments required");
            }
            format = (String) args.remove(0);
        } else {
            format = (String) firstArg;
        }
        var arguments = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            arguments[i] = args.get(i);
        }
        return String.format(locale, format, arguments);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var newArgs = new ArrayList<>(args);
        newArgs.add(pipeArg);
        return format(newArgs);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var newArgs = new ArrayList<>(args);
        return format(newArgs);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public String getName() {
        return "format";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
