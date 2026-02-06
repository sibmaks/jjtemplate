package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

/**
 * Resolves a method call when reflective method lookup fails.
 * <p>
 * {@link ReflectionUtils} invokes this resolver only as a fallback path
 * when no matching method is found by default reflection logic.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.1
 */
@FunctionalInterface
public interface MethodFallbackResolver {
    /**
     * Resolves a missing method invocation.
     *
     * @param methodName missing method name
     * @param args method arguments
     * @return fallback invocation result
     */
    Object resolve(String methodName, Object[] args);
}
