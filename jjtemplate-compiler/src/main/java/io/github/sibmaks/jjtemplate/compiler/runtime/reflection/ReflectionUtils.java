package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
                map.put(field.getName(), new AccessDescriptor(field.getName(), getter));
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
                    map.put(name, new AccessDescriptor(name, methodHandle));
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

    @AllArgsConstructor
    private static final class AccessDescriptor {
        private String name;
        private MethodHandle getter;

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
}
