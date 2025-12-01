package io.github.sibmaks.jjtemplate.evaluator.fun.impl.datetime;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.4.1
 */
public final class DateTimeNowTemplateFunction implements TemplateFunction<LocalDateTime> {

    @Override
    public LocalDateTime invoke(List<Object> args, Object pipeArg) {
        throw fail("too much arguments passed");
    }

    @Override
    public LocalDateTime invoke(List<Object> args) {
        if (!args.isEmpty()) {
            throw fail("too much arguments passed");
        }
        return LocalDateTime.now();
    }

    @Override
    public String getNamespace() {
        return "datetime";
    }

    @Override
    public String getName() {
        return "now";
    }
}
