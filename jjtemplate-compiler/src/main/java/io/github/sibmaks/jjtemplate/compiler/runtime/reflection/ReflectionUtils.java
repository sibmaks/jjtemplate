package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {
    private static final List<String> GET_METHOD_PREFIX = List.of("get", "is");
    private static final Object PROPERTY_NOT_FOUND = new Object();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, Map<String, AccessDescriptor>> PROPERTY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<AccessDescriptor>> ALL_PROPERTIES_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> METHOD_CACHE = new ConcurrentHashMap<>();

    private static Map<String, AccessDescriptor> buildDescriptorMap(Class<?> type) {
        var fields = type.getFields();
        var map = new HashMap<String, AccessDescriptor>(fields.length);
        for (var field : fields) {
            field.setAccessible(true);
            try {
                var getter = LOOKUP.unreflectGetter(field);
                map.put(field.getName(), new AccessDescriptor(field.getName(), getter, field.getType()));
            } catch (IllegalAccessException ignored) {
                // skip forbidden fields
            }
        }
        for (var method : type.getMethods()) {
            var methodName = method.getName();
            if (method.getParameterCount() > 0) {
                continue;
            }
            for (var prefix : GET_METHOD_PREFIX) {
                if (!methodName.startsWith(prefix)) {
                    continue;
                }
                var name = decapitalize(methodName.substring(prefix.length()));
                method.setAccessible(true);
                try {
                    var methodHandle = LOOKUP.unreflect(method);
                    map.put(name, new AccessDescriptor(name, methodHandle, method.getReturnType()));
                } catch (IllegalAccessException ignored) {
                    // skip forbidden methods
                }
            }
        }
        return map;
    }

    private static AccessDescriptor buildDescriptor(Class<?> type, String name) {
        var map = buildDescriptorMap(type);
        var description = map.get(name);
        if (description == null) {
            throw new TemplateEvalException(String.format("Unknown property '%s' of %s", name, type));
        }
        return description;
    }

    private static List<AccessDescriptor> buildAllDescriptors(Class<?> clazz) {
        return new ArrayList<>(buildDescriptorMap(clazz).values());
    }

    private static String decapitalize(String s) {
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static boolean isInt(String s) {
        for (var i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return !s.isEmpty();
    }

    private static Object resolveEnumConstant(Class<?> enumType, String enumValue) {
        var enumConstants = enumType.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException(enumType + " is not an enum type");
        }
        for (var constant : enumConstants) {
            var enumConstant = (Enum<?>) constant;
            if (enumConstant.name().equals(enumValue)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + enumValue);
    }

    private static ConversionResult tryConvertArgs(Class<?>[] params, List<Object> args) {
        var converted = new Object[params.length];
        var score = 0;

        for (int i = 0; i < params.length; i++) {
            var arg = i < args.size() ? args.get(i) : null;
            if (arg == null) {
                converted[i] = null;
                continue;
            }

            var paramType = wrap(params[i]);
            var argType = wrap(arg.getClass());

            if (paramType.equals(argType)) {
                converted[i] = arg;
            } else if (paramType.isEnum() && arg instanceof String) {
                converted[i] = resolveEnumConstant(paramType, (String) arg);
            } else if (paramType == Optional.class && !(arg instanceof Optional)) {
                converted[i] = Optional.of(arg);
            } else if (paramType.isAssignableFrom(argType)) {
                converted[i] = arg;
                score += 1;
            } else if (isNumeric(paramType) && isNumeric(argType)) {
                converted[i] = convertNumber((Number) arg, paramType);
                score += 2;
            } else {
                return null;
            }
        }

        return new ConversionResult(converted, score);
    }

    private static boolean isNumeric(Class<?> c) {
        return Number.class.isAssignableFrom(c)
                || c == byte.class || c == short.class
                || c == int.class || c == long.class
                || c == float.class || c == double.class;
    }

    private static Object convertNumber(Number n, Class<?> to) {
        if (to == Integer.class || to == int.class) return n.intValue();
        if (to == Long.class || to == long.class) return n.longValue();
        if (to == Double.class || to == double.class) return n.doubleValue();
        if (to == Float.class || to == float.class) return n.floatValue();
        if (to == Short.class || to == short.class) return n.shortValue();
        if (to == Byte.class || to == byte.class) return n.byteValue();
        return n;
    }

    private static Class<?> wrap(Class<?> cls) {
        if (!cls.isPrimitive()) {
            return cls;
        }
        switch (cls.getName()) {
            case "int":
                return Integer.class;
            case "boolean":
                return Boolean.class;
            case "long":
                return Long.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "char":
                return Character.class;
            case "short":
                return Short.class;
            case "byte":
                return Byte.class;
            default:
                return cls;
        }
    }

    private static Object[] convertVarARgs(
            List<Object> args,
            Method bestMatch,
            Object[] bestConverted
    ) {
        var paramTypes = bestMatch.getParameterTypes();
        var normalCount = paramTypes.length - 1;
        var newArgs = new Object[paramTypes.length];
        System.arraycopy(bestConverted, 0, newArgs, 0, normalCount);

        var varargType = paramTypes[normalCount].getComponentType();
        var varargArray = Array.newInstance(varargType, args.size() - normalCount);

        for (var i = normalCount; i < args.size(); i++) {
            Array.set(varargArray, i - normalCount, args.get(i));
        }
        newArgs[normalCount] = varargArray;
        bestConverted = newArgs;
        return bestConverted;
    }

    private static String getStringChar(CharSequence seq, int idx) {
        if (idx < 0 || idx >= seq.length()) {
            throw new TemplateEvalException("String index out of range: " + idx);
        }
        return Character.toString(seq.charAt(idx));
    }

    private static Object getListItem(List<?> list, int idx) {
        if (idx < 0 || idx >= list.size()) {
            throw new TemplateEvalException("List index out of range: " + idx);
        }
        return list.get(idx);
    }

    private static Object getArrayItem(Object obj, int idx) {
        var len = Array.getLength(obj);
        if (idx < 0 || idx >= len) {
            throw new TemplateEvalException("Array index out of range: " + idx);
        }
        return Array.get(obj, idx);
    }

    private static Object getProperty(Object obj, String name, Class<?> type) {
        var map = PROPERTY_CACHE.computeIfAbsent(type, c -> new ConcurrentHashMap<>());
        var desc = map.computeIfAbsent(name, n -> buildDescriptor(type, n));
        try {
            return desc.get(obj);
        } catch (Throwable e) {
            throw new TemplateEvalException("Failed to access property '" + name + "' of " + type, e);
        }
    }

    private static boolean isMethodCompatible(Method method, List<Class<?>> argTypes) {
        var params = method.getParameterTypes();
        if (!method.isVarArgs()) {
            if (params.length != argTypes.size()) {
                return false;
            }
            for (int i = 0; i < params.length; i++) {
                var argType = argTypes.get(i);
                if (argType == null) {
                    continue;
                }
                var paramType = wrap(params[i]);
                var wrappedArgType = wrap(argType);
                if (paramType.equals(wrappedArgType) || paramType.isAssignableFrom(wrappedArgType)) {
                    continue;
                }
                if (isNumeric(paramType) && isNumeric(wrappedArgType)) {
                    continue;
                }
                return false;
            }
            return true;
        }

        var fixedCount = params.length - 1;
        if (argTypes.size() < fixedCount) {
            return false;
        }
        for (int i = 0; i < fixedCount; i++) {
            var argType = argTypes.get(i);
            if (argType == null) {
                continue;
            }
            var paramType = wrap(params[i]);
            var wrappedArgType = wrap(argType);
            if (paramType.equals(wrappedArgType) || paramType.isAssignableFrom(wrappedArgType)) {
                continue;
            }
            if (isNumeric(paramType) && isNumeric(wrappedArgType)) {
                continue;
            }
            return false;
        }
        var varArgType = wrap(params[fixedCount].getComponentType());
        for (int i = fixedCount; i < argTypes.size(); i++) {
            var argType = argTypes.get(i);
            if (argType == null) {
                continue;
            }
            var wrappedArgType = wrap(argType);
            if (varArgType.equals(wrappedArgType) || varArgType.isAssignableFrom(wrappedArgType)) {
                continue;
            }
            if (isNumeric(varArgType) && isNumeric(wrappedArgType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @AllArgsConstructor
    private static final class AccessDescriptor {
        private String name;
        private MethodHandle getter;
        private Class<?> valueType;

        Object get(Object target) throws Throwable {
            return getter == null ? null : getter.invoke(target);
        }
    }

    private static final class ConversionResult {
        final Object[] values;
        final int score;

        ConversionResult(Object[] values, int score) {
            this.values = values;
            this.score = score;
        }
    }

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
            var params = method.getParameterTypes();
            Method selected = method;
            Object[] converted;
            if (method.isVarArgs()) {
                int fixedCount = params.length - 1;
                var varargType = params[fixedCount].getComponentType();
                var varargArray = Array.newInstance(varargType, args.size() - fixedCount);
                for (int i = fixedCount; i < args.size(); i++) {
                    Array.set(varargArray, i - fixedCount, args.get(i));
                }
                var merged = new ArrayList<>(args.subList(0, fixedCount));
                merged.add(varargArray);
                var conversion = tryConvertArgs(params, merged);
                if (conversion == null) {
                    throw new TemplateEvalException("No matching method " + method.getName() + " found for args " + args);
                }
                converted = conversion.values;
                converted = convertVarARgs(args, selected, converted);
            } else {
                var conversion = tryConvertArgs(params, args);
                if (conversion == null) {
                    throw new TemplateEvalException("No matching method " + method.getName() + " found for args " + args);
                }
                converted = conversion.values;
            }
            selected.setAccessible(true);
            return selected.invoke(target, converted);
        }
    }

    /**
     * Returns all readable properties of an object as a map.
     *
     * @param obj source object
     * @return map of property names to values
     */
    public static Map<String, Object> getAllProperties(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        var type = obj.getClass();

        if (obj instanceof Map<?, ?>) {
            var map = (Map<?, ?>) obj;
            var result = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                if (entry.getKey() instanceof String) {
                    var key = (String) entry.getKey();
                    result.put(key, entry.getValue());
                }
            }
            return result;
        }

        if (obj instanceof List<?>) {
            var list = (List<?>) obj;
            var result = new LinkedHashMap<String, Object>();
            for (int i = 0; i < list.size(); i++) {
                result.put(String.valueOf(i), list.get(i));
            }
            return result;
        }

        var descriptors = ALL_PROPERTIES_CACHE.computeIfAbsent(type, ReflectionUtils::buildAllDescriptors);
        var map = new LinkedHashMap<String, Object>(descriptors.size());
        for (var desc : descriptors) {
            try {
                map.put(desc.name, desc.get(obj));
            } catch (Throwable ignored) {
                // ignore unavailable fields
            }
        }
        return map;
    }

    /**
     * Resolves a single property from an object, map, list, array, or custom resolver.
     *
     * @param obj  source object
     * @param name property name or index
     * @return resolved property value, or {@code null} when not found by supported fallback paths
     */
    public static Object getProperty(Object obj, String name) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof FieldResolver) {
            var resolver = (FieldResolver) obj;
            return resolver.resolve(name);
        }

        if (obj instanceof Map<?, ?>) {
            var map = (Map<?, ?>) obj;
            if (map.containsKey(name)) {
                return map.get(name);
            }
            if (obj instanceof FieldFallbackResolver) {
                var fallbackResolver = (FieldFallbackResolver) obj;
                return fallbackResolver.resolve(name);
            }
            return null;
        }

        Object property = PROPERTY_NOT_FOUND;
        var type = obj.getClass();
        if (isInt(name)) {
            var idx = Integer.parseInt(name);
            if (obj.getClass().isArray()) {
                property = getArrayItem(obj, idx);
            } else if (obj instanceof List<?>) {
                property = getListItem((List<?>) obj, idx);
            } else if (obj instanceof CharSequence) {
                property = getStringChar((CharSequence) obj, idx);
            }
        }
        if (property != PROPERTY_NOT_FOUND) {
            return property;
        }

        try {
            return getProperty(obj, name, type);
        } catch (TemplateEvalException e) {
            var unknownPropertyMessage = "Unknown property '" + name + "' of " + type;
            if (!unknownPropertyMessage.equals(e.getMessage())) {
                throw e;
            }
            if (obj instanceof FieldFallbackResolver) {
                var fallbackResolver = (FieldFallbackResolver) obj;
                return fallbackResolver.resolve(name);
            }
            throw e;
        }
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
        if (obj == null) {
            return null;
        }
        for (var property : properties) {
            if (!property.getOwnerType().isInstance(obj)) {
                continue;
            }
            try {
                return property.get(obj);
            } catch (Throwable e) {
                throw new TemplateEvalException("Failed to access property '" + name + "' of " + obj.getClass(), e);
            }
        }
        return getProperty(obj, name);
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
        if (target == null) {
            throw new TemplateEvalException("Cannot call method on null target");
        }

        if (target instanceof MethodResolver) {
            var resolver = (MethodResolver) target;
            return resolver.resolve(methodName, args.toArray());
        }

        var type = target.getClass();
        var methods = METHOD_CACHE.computeIfAbsent(type, Class::getMethods);

        Method bestMatch = null;
        Object[] bestConverted = null;
        var bestScore = Integer.MAX_VALUE;
        for (var m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            var params = m.getParameterTypes();

            var varArgs = m.isVarArgs();
            if (varArgs ? args.size() >= params.length - 1 : params.length == args.size()) {
                var argsToConvert = args;
                if (varArgs) {
                    int fixedCount = params.length - 1;
                    var varargType = params[fixedCount].getComponentType();
                    var varargArray = Array.newInstance(varargType, args.size() - fixedCount);
                    for (int i = fixedCount; i < args.size(); i++) {
                        Array.set(varargArray, i - fixedCount, args.get(i));
                    }
                    var merged = new ArrayList<>(args.subList(0, fixedCount));
                    merged.add(varargArray);
                    argsToConvert = merged;
                }

                var conversion = tryConvertArgs(params, argsToConvert);
                if (conversion != null && conversion.score < bestScore) {
                    bestMatch = m;
                    bestConverted = conversion.values;
                    bestScore = conversion.score;
                }
            }
        }


        if (bestMatch == null) {
            if (target instanceof MethodFallbackResolver) {
                var fallbackResolver = (MethodFallbackResolver) target;
                return fallbackResolver.resolve(methodName, args.toArray());
            }
            throw new TemplateEvalException("No matching method " + methodName + " found for args " + args);
        }

        if (bestMatch.isVarArgs()) {
            bestConverted = convertVarARgs(args, bestMatch, bestConverted);
        }

        try {
            bestMatch.setAccessible(true);
            return bestMatch.invoke(target, bestConverted);
        } catch (ReflectiveOperationException e) {
            throw new TemplateEvalException("Error invoking method " + methodName + ": " + e.getMessage(), e);
        }
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
        if (target == null) {
            throw new TemplateEvalException("Cannot call method on null target");
        }
        for (var resolvedMethod : resolvedMethods) {
            if (!resolvedMethod.getOwnerType().isInstance(target)) {
                continue;
            }
            try {
                return resolvedMethod.invoke(target, args);
            } catch (ReflectiveOperationException e) {
                throw new TemplateEvalException("Error invoking method " + methodName + ": " + e.getMessage(), e);
            }
        }
        return invokeMethodReflective(target, methodName, args);
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
        var methods = METHOD_CACHE.computeIfAbsent(type, Class::getMethods);
        var result = new ArrayList<ResolvedMethod>();
        for (var method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (!isMethodCompatible(method, argTypes)) {
                continue;
            }
            result.add(new ResolvedMethod(type, method));
        }
        return result;
    }

    /**
     * Resolves a readable property for the declared type.
     *
     * @param type         owner type
     * @param propertyName property name
     * @return resolved property if available
     */
    public static Optional<ResolvedProperty> resolveProperty(Class<?> type, String propertyName) {
        if (type == null || type.isArray() || Map.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)) {
            return Optional.empty();
        }
        var descriptors = buildDescriptorMap(type);
        var descriptor = descriptors.get(propertyName);
        if (descriptor == null) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedProperty(type, propertyName, descriptor.getter, descriptor.valueType));
    }
}
