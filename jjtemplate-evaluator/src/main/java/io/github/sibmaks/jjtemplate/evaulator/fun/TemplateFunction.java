package io.github.sibmaks.jjtemplate.evaulator.fun;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public interface TemplateFunction {

    ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg);

    String getName();

    default ExpressionValue first(List<ExpressionValue> args, ExpressionValue pipe) {
        return !args.isEmpty() ? args.get(0) : pipe;
    }
}
