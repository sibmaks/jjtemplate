package io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.lexer.api.Reserved;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public final class BooleanTemplateFunction implements TemplateFunction<Boolean> {

    private static Boolean convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            var strValue = (String) value;
            if (Reserved.TRUE.eq(strValue)) {
                return true;
            }
            if (Reserved.FALSE.eq(strValue)) {
                return false;
            }
        }
        throw new TemplateEvalException("boolean: cannot convert: " + value);
    }

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("boolean: too much arguments passed");
        }
        return convert(pipeArg);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("boolean: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("boolean: too much arguments passed");
        }
        var arg = args.get(0);
        return convert(arg);
    }

    @Override
    public String getName() {
        return "boolean";
    }
}
