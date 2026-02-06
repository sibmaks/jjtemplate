package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

/**
 * Resolves a field value using custom logic.
 * <p>
 * If a target object implements this interface, {@link ReflectionUtils}
 * uses this resolver instead of default field/property lookup.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.1
 */
@FunctionalInterface
public interface FieldResolver {
    /**
     * Resolves a field by name.
     *
     * @param fieldName field/property name
     * @return resolved value
     */
    Object resolve(String fieldName);
}
