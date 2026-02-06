package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

/**
 * Resolves a method call using custom logic.
 * <p>
 * If a target object implements this interface, {@link ReflectionUtils}
 * uses this resolver instead of reflective method lookup.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.1
 */
@FunctionalInterface
public interface MethodResolver {
    /**
     * Resolves a method invocation.
     *
     * @param methodName method name
     * @param args method arguments
     * @return invocation result
     */
    Object resolve(String methodName, Object[] args);
}
