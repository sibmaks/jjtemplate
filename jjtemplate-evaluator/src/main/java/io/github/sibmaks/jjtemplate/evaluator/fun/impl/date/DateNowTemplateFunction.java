package io.github.sibmaks.jjtemplate.evaluator.fun.impl.date;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDate;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.1
 */
public final class DateNowTemplateFunction implements TemplateFunction<LocalDate> {

    @Override
    public LocalDate invoke(List<Object> args, Object pipeArg) {
        throw fail("too much arguments passed");
    }

    @Override
    public LocalDate invoke(List<Object> args) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return LocalDate.now();
    }

    @Override
    public String getNamespace() {
        return "date";
    }

    @Override
    public String getName() {
        return "now";
    }
}
