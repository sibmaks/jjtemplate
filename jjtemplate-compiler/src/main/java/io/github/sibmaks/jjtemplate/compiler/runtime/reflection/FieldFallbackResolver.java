package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

/**
 * Resolves a field value when default field/property lookup fails.
 * <p>
 * {@link ReflectionUtils} invokes this resolver only as a fallback path
 * when no default field/property can be resolved.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.1
 */
@FunctionalInterface
public interface FieldFallbackResolver {
    /**
     * Resolves a missing field by name.
     *
     * @param fieldName missing field/property name
     * @return fallback value
     */
    Object resolve(String fieldName);
}
