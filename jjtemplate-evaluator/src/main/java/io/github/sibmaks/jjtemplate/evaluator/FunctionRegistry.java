package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast.BooleanTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast.FloatTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast.IntTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.cast.StrTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.logic.*;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.math.NegTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.FormatStringTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringLowerTemplateFunction;
import io.github.sibmaks.jjtemplate.evaluator.fun.impl.string.StringUpperTemplateFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all available template functions.
 * <p>
 * The registry aggregates both <b>built-in</b> and <b>user-provided</b>
 * {@link TemplateFunction}s into a single lookup table used during expression
 * evaluation by the {@link TemplateEvaluator}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Registers all standard JJTemplate functions (math, logic, string, date, etc.).</li>
 *   <li>Registers custom user-defined functions from {@link TemplateEvaluationOptions}.</li>
 *   <li>Prevents duplicate function name conflicts.</li>
 *   <li>Provides lookup for functions by name at runtime.</li>
 * </ul>
 *
 * <h2>Built-in Functions</h2>
 * <p>
 * The following categories of built-in functions are available:
 * </p>
 * <ul>
 *   <li><b>Type conversion:</b> {@code int}, {@code float}, {@code boolean}, {@code str}</li>
 *   <li><b>String operations:</b> {@code upper}, {@code lower}, {@code concat}, {@code format}</li>
 *   <li><b>Collection utilities:</b> {@code list}, {@code len}, {@code empty}, {@code collapse}, {@code contains}</li>
 *   <li><b>Math / logic:</b> {@code eq}, {@code neq}, {@code lt}, {@code le}, {@code gt}, {@code ge},
 *       {@code and}, {@code or}, {@code xor}, {@code not}, {@code neg}</li>
 *   <li><b>Date operations:</b> {@code formatDate}, {@code parseDate}, {@code parseDateTime}</li>
 *   <li><b>Default / fallback:</b> {@code default}</li>
 * </ul>
 *
 * <h2>Customization</h2>
 * <p>
 * Users can register custom functions through
 * {@link TemplateEvaluationOptions.TemplateEvaluationOptionsBuilder#functions(List)}, which will override
 * built-in ones if the same name is used.
 * </p>
 *
 * <h2>Error Handling</h2>
 * If a requested function is not found, {@link TemplateEvalException} is thrown.
 *
 * @author sibmaks
 * @see TemplateEvaluator
 * @see TemplateEvaluationOptions
 * @see TemplateFunction
 * @since 0.1.2
 */
final class FunctionRegistry {
    private final Map<String, TemplateFunction<?>> functions;

    /**
     * Constructs a function registry using the provided evaluation options.
     * <p>
     * Registers all built-in functions, then merges user-defined ones.
     * If a user function has the same name as a built-in one, an
     * {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param options evaluation options containing locale and custom function definitions
     * @throws IllegalArgumentException if duplicate function names are detected
     */
    FunctionRegistry(TemplateEvaluationOptions options) {
        var builtInFunctions = getBuiltInFunctions();
        var userFunctions = options.getFunctions();
        this.functions = new HashMap<>(builtInFunctions.size() + userFunctions.size());
        for (var function : builtInFunctions) {
            functions.put(function.getName(), function);
        }
        for (var function : userFunctions) {
            var functionName = function.getName();
            var overwritten = functions.put(functionName, function);
            if (overwritten != null) {
                throw new IllegalArgumentException("Duplicate function name: " + functionName);
            }
        }
    }

    /**
     * Provides a predefined list of built-in JJTemplate functions.
     * <p>
     * The returned list includes type conversions, logical operations, string formatting,
     * date/time utilities, and collection helpers.
     * </p>
     *
     * @return an immutable list of built-in {@link TemplateFunction}s
     */
    private static List<TemplateFunction<?>> getBuiltInFunctions() {
        return List.of(
                // Type conversion
                new BooleanTemplateFunction(),
                new FloatTemplateFunction(),
                new IntTemplateFunction(),
                new StrTemplateFunction(),
                // String and formatting
                new ConcatTemplateFunction(),
                new StringLowerTemplateFunction(),
                new StringUpperTemplateFunction(),
                new FormatStringTemplateFunction(),
                // Collections & String & Objects
                new ContainsTemplateFunction(),
                new EmptyTemplateFunction(),
                new LengthTemplateFunction(),
                new ListTemplateFunction(),
                new CollapseTemplateFunction(),
                // Logic and comparison
                new EqualsTemplateFunction(),
                new NotEqualsTemplateFunction(),
                new NotTemplateFunction(),
                new DefaultTemplateFunction(),
                new LTCompareTemplateFunction(),
                new LECompareTemplateFunction(),
                new GTCompareTemplateFunction(),
                new GECompareTemplateFunction(),
                new AndTemplateFunction(),
                new OrTemplateFunction(),
                new XorTemplateFunction(),
                // Date and time
                new FormatDateTemplateFunction(),
                new ParseDateTemplateFunction(),
                new ParseDateTimeTemplateFunction(),
                // Math
                new NegTemplateFunction(),
                // Utils
                new LocaleTemplateFunction()
        );
    }

    /**
     * Retrieves a template function by its name.
     *
     * @param functionName the name of the function to look up
     * @return the {@link TemplateFunction} associated with the given name
     * @throws TemplateEvalException if no function with the specified name exists
     */
    @SuppressWarnings("unchecked")
    public <T> TemplateFunction<T> getFunction(String functionName) {
        var templateFunction = functions.get(functionName);
        if (templateFunction == null) {
            throw new TemplateEvalException("Function '" + functionName + "' not found");
        }
        return (TemplateFunction<T>) templateFunction;
    }

}
