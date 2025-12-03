package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.date;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Template function that formats date values using a pattern.
 * <p>
 * Supports {@link TemporalAccessor} and {@link Date}.
 * Locale may be provided explicitly, otherwise the default locale is used.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class DateFormatTemplateFunction implements TemplateFunction<String> {

    private String format(Locale locale, String format, Object date) {
        if (date == null) {
            return null;
        }
        if (date instanceof TemporalAccessor) {
            var formatter = DateTimeFormatter.ofPattern(format, locale);
            return formatter.format((TemporalAccessor) date);
        }
        if (date instanceof Date) {
            var formatter = new SimpleDateFormat(format, locale);
            return formatter.format((Date) date);
        }
        throw fail("cannot convert " + date + " to TemporalAccessor");
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var locale = Locale.getDefault();
        String format;
        if (args.get(0) instanceof Locale) {
            locale = (Locale) args.get(0);
            if (args.size() < 2) {
                throw fail("at least 2 arguments required");
            }
            format = (String) args.get(1);
        } else {
            if (args.size() != 1) {
                throw fail("1 argument required");
            }
            format = (String) args.get(0);
        }
        return format(locale, format, pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw fail("at least 2 arguments required");
        }
        var locale = Locale.getDefault();
        String format;
        Object date;
        if (args.get(0) instanceof Locale) {
            locale = (Locale) args.get(0);
            if (args.size() < 3) {
                throw fail("at least 3 arguments required");
            }
            format = (String) args.get(1);
            date = args.get(2);
        } else {
            format = (String) args.get(0);
            date = args.get(1);
        }
        return format(locale, format, date);
    }

    @Override
    public String getNamespace() {
        return "date";
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
