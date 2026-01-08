package io.github.sibmaks.jjtemplate.compiler.runtime.expression.function;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface FunctionCallTemplateExpression extends TemplateExpression {

    /**
     * Function to call on template evaluation
     *
     * @param <T> function return type
     * @return template function instance
     */
    <T> TemplateFunction<T> getFunction();

    /**
     * Evaluate function arguments
     *
     * @param context evaluation context used to resolve arguments
     * @return evaluated function arguments
     */
    List<Object> getArguments(Context context);

    @Override
    default Object apply(final Context context) {
        var function = getFunction();
        var args = getArguments(context);
        return function.invoke(args);
    }

    /**
     * Evaluates this function call as a pipe operation.
     * <p>
     * All argument expressions are evaluated against the provided context,
     * and the resulting values are passed to the function along with
     * the supplied pipe value.
     * </p>
     *
     * @param context evaluation context for resolving variables
     * @param pipe    value coming from the left side of a pipe chain
     * @return result of invoking the function with evaluated arguments and pipe value
     */
    default Object apply(final Context context, final Object pipe) {
        var function = getFunction();
        var args = getArguments(context);
        return function.invoke(args, pipe);
    }

}
