package io.github.sibmaks.jjtemplate.evaluator.fun.impl.locale;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
 * Template function that creates a new {@link Locale} instance.
 *
 * <p>Supports all standard {@code Locale} constructors and accepts
 * language, country, and variant values in both direct and pipe forms.</p>
 *
 * @author sibmaks
 * @since 0.3.0
 */
public final class LocaleNewTemplateFunction implements TemplateFunction<Locale> {

    @Override
    public Locale invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            if (pipeArg instanceof Locale) {
                return (Locale) pipeArg;
            }
            return new Locale(pipeArg.toString());
        }
        var size = args.size();
        var language = (String) args.get(0);
        if (size == 1) {
            var country = (String) pipeArg;
            return new Locale(language, country);
        }
        if (size == 2) {
            var country = (String) args.get(1);
            var variant = (String) pipeArg;
            return new Locale(language, country, variant);
        }
        throw fail("too much arguments passed");
    }

    @Override
    public Locale invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var size = args.size();
        if (size == 1) {
            var arg = args.get(0);
            if (arg instanceof Locale) {
                return (Locale) arg;
            }
            return new Locale(arg.toString());
        }
        var language = (String) args.get(0);
        var country = (String) args.get(1);
        if (size == 2) {
            return new Locale(language, country);
        }
        if (size == 3) {
            var variant = (String) args.get(2);
            return new Locale(language, country, variant);
        }
        throw fail("too much arguments passed");
    }

    @Override
    public String getNamespace() {
        return "locale";
    }

    @Override
    public String getName() {
        return "new";
    }
}
