package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.exception.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

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
@Slf4j
final class FunctionRegistry {
    private final Map<String, Map<String, TemplateFunction<?>>> functions;

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
        addFunctions(builtInFunctions);
        addFunctions(userFunctions);
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
        var result = new ArrayList<TemplateFunction<?>>();
        var found = new HashSet<String>();
        var basePackage = "io.github.sibmaks.jjtemplate.evaluator.fun.impl";

        for (var type : ClasspathScanner.findClasses(basePackage)) {
            if (!TemplateFunction.class.isAssignableFrom(type) ||
                    Modifier.isAbstract(type.getModifiers()) ||
                    Modifier.isInterface(type.getModifiers())) {
                continue;
            }
            if (!found.add(type.getName())) {
                log.warn("Duplicate built-in function class found: {}", type.getName());
                continue;
            }
            try {
                var castType = (Class<TemplateFunction<?>>) type;
                var defaultConstructor = castType.getDeclaredConstructor();
                defaultConstructor.setAccessible(true);
                var instance = defaultConstructor.newInstance();
                result.add(instance);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(String.format("Failed to create built-in function: %s", type.getName()), e);
            }
        }

        return result;
    }

    private void addFunctions(List<TemplateFunction<?>> builtInFunctions) {
        for (var function : builtInFunctions) {
            var functionName = function.getName();
            var namespaceFunctions = functions.computeIfAbsent(function.getNamespace(), k -> new HashMap<>());
            var overwritten = namespaceFunctions.put(functionName, function);
            if (overwritten != null) {
                throw new IllegalArgumentException(String.format("Duplicate function name: %s:%s", function.getNamespace(), functionName));
            }
        }
    }

    /**
     * Retrieves a template function by its name.
     *
     * @param namespace the name of the function namespace to look up
     * @param name      the name of the function to look up
     * @return the {@link TemplateFunction} associated with the given name
     * @throws TemplateEvalException if no function with the specified name exists
     */
    @SuppressWarnings("unchecked")
    public <T> TemplateFunction<T> getFunction(String namespace, String name) {
        var namespaceFunctions = functions.get(namespace);
        if (namespaceFunctions == null) {
            throw new TemplateEvalException(String.format("No such namespace: '%s'", namespace));
        }
        var templateFunction = namespaceFunctions.get(name);
        if (templateFunction == null) {
            throw new TemplateEvalException(String.format("Function '%s' not found in namespace: '%s'", name, namespace));
        }
        return (TemplateFunction<T>) templateFunction;
    }

}
