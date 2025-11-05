package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class GECompareTemplateFunction extends CompareTemplateFunction {

    @Override
    public Boolean invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("ge: 1 argument required");
        }
        var y = args.get(0);
        return fnCmp(pipeArg, y, 1, true);
    }

    @Override
    public Boolean invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("ge: 2 arguments required");
        }
        var x = args.get(0);
        var y = args.get(1);
        return fnCmp(x, y, 1, true);
    }

    @Override
    public String getName() {
        return "ge";
    }
}
