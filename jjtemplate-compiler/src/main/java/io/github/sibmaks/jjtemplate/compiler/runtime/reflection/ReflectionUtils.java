package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reflection facade for property and method access used during template evaluation.
 *
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * Pre-resolved property accessor for a specific declared type.
     */
    @AllArgsConstructor
    public static final class ResolvedProperty {
        @Getter
        private final Class<?> ownerType;
        @Getter
        private final String propertyName;
        private final MethodHandle getter;
        @Getter
        private final Class<?> valueType;

        /**
         * Reads the property value from the target object.
         *
         * @param target receiver object
         * @return resolved property value
         * @throws Throwable when invocation fails
         */
        Object get(Object target) throws Throwable {
            return getter.invoke(target);
        }
    }

    /**
     * Pre-resolved method wrapper for a specific declared type.
     */
    @Getter
    @AllArgsConstructor
    public static final class ResolvedMethod {
        private final Class<?> ownerType;
        private final Method method;

        /**
         * Returns the declared return type of the resolved method.
         *
         * @return declared return type
         */
        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        Object invoke(Object target, List<Object> args) throws ReflectiveOperationException {
            return ReflectionMethodSupport.invokeResolvedMethod(method, target, args);
        }
    }

    /**
     * Returns all readable properties of an object as a map.
     *
     * @param obj source object
     * @return map of property names to values
     */
    public static Map<String, Object> getAllProperties(Object obj) {
        return ReflectionPropertySupport.getAllProperties(obj);
    }

    /**
     * Resolves a single property from an object, map, list, array, or custom resolver.
     *
     * @param obj  source object
     * @param name property name or index
     * @return resolved property value, or {@code null} when not found by supported fallback paths
     */
    public static Object getProperty(Object obj, String name) {
        return ReflectionPropertySupport.getProperty(obj, name);
    }

    /**
     * Resolves a property using precomputed accessors when possible.
     *
     * @param obj        source object
     * @param name       property name
     * @param properties pre-resolved accessors
     * @return resolved property value
     */
    public static Object getProperty(
            Object obj,
            String name,
            List<ResolvedProperty> properties
    ) {
        return ReflectionPropertySupport.getProperty(obj, name, properties);
    }

    /**
     * Invokes a method on the target using reflective overload resolution and argument conversion.
     *
     * @param target     invocation target
     * @param methodName method name
     * @param args       invocation arguments
     * @return invocation result
     */
    public static Object invokeMethodReflective(Object target, String methodName, List<Object> args) {
        return ReflectionMethodSupport.invokeMethodReflective(target, methodName, args);
    }

    /**
     * Invokes a method using pre-resolved handlers when possible.
     *
     * @param target          invocation target
     * @param methodName      method name
     * @param args            invocation arguments
     * @param resolvedMethods pre-resolved methods
     * @return invocation result
     */
    public static Object invokeMethodReflective(
            Object target,
            String methodName,
            List<Object> args,
            List<ResolvedMethod> resolvedMethods
    ) {
        return ReflectionMethodSupport.invokeMethodReflective(target, methodName, args, resolvedMethods);
    }

    /**
     * Returns whether the declared type may be extended by a runtime subtype.
     *
     * @param type declared type
     * @return {@code true} when soft validation should treat missing members as unknown
     */
    public static boolean isSoftlyExtensible(Class<?> type) {
        if (type.isPrimitive() || type.isArray() || type.isEnum()) {
            return false;
        }
        var modifiers = type.getModifiers();
        return !java.lang.reflect.Modifier.isFinal(modifiers);
    }

    /**
     * Resolves compatible methods for the declared type and argument types.
     *
     * @param type       owner type
     * @param methodName method name
     * @param argTypes   argument types; {@code null} means unknown
     * @return compatible methods
     */
    public static List<ResolvedMethod> resolveMethods(
            Class<?> type,
            String methodName,
            List<Class<?>> argTypes
    ) {
        return ReflectionMethodSupport.resolveMethods(type, methodName, argTypes);
    }

    /**
     * Resolves a readable property for the declared type.
     *
     * @param type         owner type
     * @param propertyName property name
     * @return resolved property if available
     */
    public static Optional<ResolvedProperty> resolveProperty(Class<?> type, String propertyName) {
        return ReflectionPropertySupport.resolveProperty(type, propertyName);
    }

    private static Object resolveEnumConstant(Class<?> enumType, String enumValue) {
        return ReflectionConversionSupport.resolveEnumConstant(enumType, enumValue);
    }

    static TemplateEvalException methodInvocationError(String methodName, ReflectiveOperationException exception) {
        return new TemplateEvalException("Error invoking method " + methodName + ": " + exception.getMessage(), exception);
    }
}
