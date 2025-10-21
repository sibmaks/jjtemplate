package io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author sibmaks
 */
public class EqualsTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.size() == 1) {
            return ExpressionValue.of(
                    Objects.equals(
                            args.get(0).getValue(),
                            pipeArg.getValue()
                    )
            );
        }
        if (args.size() == 2) {
            return ExpressionValue.of(
                    Objects.equals(
                            args.get(0).getValue(),
                            args.get(1).getValue()
                    )
            );
        }
        throw new TemplateEvalException("eq: expected 1 or 2 args");
    }

    @Override
    public String getName() {
        return "eq";
    }
}
