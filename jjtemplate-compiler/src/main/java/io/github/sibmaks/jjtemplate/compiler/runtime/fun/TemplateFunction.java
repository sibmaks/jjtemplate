package io.github.sibmaks.jjtemplate.compiler.runtime.fun;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateFunctionEvalException;

import java.util.List;

/**
 * Represents a callable function within the JJTemplate evaluation engine.
 * <p>
 * Template functions are reusable operations that can be invoked directly
 * or chained using the pipe operator (<code>|</code>) inside template expressions.
 * Implementations define custom transformation, logic, or computation behavior
 * available during template evaluation.
 * </p>
 *
 * <h2>Function Signature</h2>
 * <p>
 * Each function receives:
 * </p>
 * <ul>
 *   <li>a list of positional arguments ({@link #invoke(List, Object) args})</li>
 *   <li>an optional <em>pipe argument</em> — a value passed from the left side of a pipe expression</li>
 * </ul>
 *
 * <h2>Invocation Forms</h2>
 * <ul>
 *   <li><b>Direct call:</b> {@code function arg1, arg2}</li>
 *   <li><b>Pipe call:</b> {@code expression | function arg1, arg2}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre><code>
 * public class AddTemplateFunction implements TemplateFunction&lt;Object&gt; {
 *     &#64;Override
 *     public Object invoke(List&lt;Object&gt; args, Object pipeArg) {
 *         double a = ((Number) args.get(0)).doubleValue();
 *         double b = ((Number) pipeArg).doubleValue();
 *         return a + b;
 *     }
 *
 *     &#64;Override
 *     public Object invoke(List&lt;Object&gt; args) {
 *         double a = ((Number) args.get(0)).doubleValue();
 *         double b = ((Number) args.get(1)).doubleValue();
 *     }
 *
 *     &#64;Override
 *     public String getName() {
 *         return "add";
 *     }
 * }
 * </code></pre>
 *
 * <p>All implementations must be thread-safe and side-effect free.</p>
 *
 * @param <T> the result type of the function
 * @author sibmaks
 * @since 0.0.1
 */
public interface TemplateFunction<T> {

    /**
     * Invokes this function with the specified arguments and an optional
     * <em>pipe argument</em> (value passed from a left-hand expression).
     *
     * @param args    list of positional arguments for the function
     * @param pipeArg value from a preceding pipe expression (may be {@code null})
     * @return the computed result of the function
     */
    T invoke(List<Object> args, Object pipeArg);

    /**
     * Invokes this function with the specified arguments only,
     * without a pipe argument.
     *
     * @param args list of positional arguments
     * @return the computed result of the function
     */
    T invoke(List<Object> args);

    /**
     * Returns the namespace of the function, or an empty string if none.
     *
     * @return namespace string
     */
    String getNamespace();

    /**
     * Returns the unique name used to reference this function in templates.
     * <p>
     * Function names must be lowercase and unique within a single evaluation context.
     * </p>
     *
     * @return the function name
     */
    String getName();

    /**
     * Creates an exception for signaling a function evaluation error.
     *
     * @param message error description
     * @return exception instance
     */
    default TemplateEvalException fail(String message) {
        return new TemplateFunctionEvalException(this, message);
    }

    /**
     * Creates an exception for signaling a function evaluation error.
     *
     * @param message error description
     * @param cause   underlying cause
     * @return exception instance
     */
    default TemplateEvalException fail(String message, Exception cause) {
        return new TemplateFunctionEvalException(this, message, cause);
    }

    /**
     * Indicates whether this function should be evaluated dynamically
     * during template processing.
     * <p>
     * Dynamic functions are invoked every time they appear in an expression,
     * even if all their inputs are constant. Static (non-dynamic) functions
     * may be eligible for compile-time constant folding and other optimizations.
     * </p>
     *
     * @return {@code true} if the function must always be evaluated at runtime;
     * {@code false} if it may be treated as a pure/static function
     */
    default boolean isDynamic() {
        return true;
    }
}
