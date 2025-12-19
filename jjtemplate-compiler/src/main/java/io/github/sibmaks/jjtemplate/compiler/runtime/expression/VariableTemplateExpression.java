package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Represents a variable reference inside a template expression.
 * <p>
 * A variable consists of a root name and an optional call chain that applies
 * property accessors or method invocations. During evaluation a variable is
 * resolved against the {@link Context} and each chain step transforms the
 * intermediate value until the final result is produced.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class VariableTemplateExpression implements TemplateExpression {
    private final String rootName;
    private final List<Chain> callChain;

    @Override
    public Object apply(final Context context) {
        var root = context.getRoot(rootName);
        for (var chain : callChain) {
            if (root == null) {
                return null;
            }
            root = chain.apply(context, root);
        }
        return root;
    }

    @Override
    public <T> T visit(TemplateExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * A single transformation step applied to the current variable value.
     * <p>
     * Implementations may extract a property, invoke a method or perform any
     * other context-dependent transformation on the intermediate value.
     * </p>
     */
    @FunctionalInterface
    public interface Chain extends BiFunction<Context, Object, Object> {

    }

    /**
     * Chain element that resolves a property from the current value using reflection.
     * <p>
     * If the property does not exist or cannot be read, {@code null} is returned.
     * </p>
     */
    @AllArgsConstructor
    @ToString
    public final static class GetPropertyChain implements Chain {
        private final String propertyName;

        @Override
        public Object apply(final Context context, final Object o) {
            return ReflectionUtils.getProperty(o, propertyName);
        }
    }

    /**
     * Chain element that invokes a method on the current value.
     * <p>
     * Arguments of the method call are template expressions that are evaluated
     * before method resolution. Reflection is used to locate and invoke the method.
     * </p>
     */
    @Getter
    @AllArgsConstructor
    @ToString
    public final static class CallMethodChain implements Chain {
        private final String methodName;
        private final List<TemplateExpression> argsExpressions;

        @Override
        public Object apply(final Context context, final Object o) {
            var args = argsExpressions.stream()
                    .map(it -> it.apply(context))
                    .collect(Collectors.toList());
            return ReflectionUtils.invokeMethodReflective(o, methodName, args);
        }
    }
}
