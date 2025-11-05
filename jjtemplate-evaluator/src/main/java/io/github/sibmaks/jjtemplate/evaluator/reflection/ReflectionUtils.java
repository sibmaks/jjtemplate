package io.github.sibmaks.jjtemplate.evaluator.reflection;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
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
 *
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, Map<String, AccessDescriptor>> PROPERTY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<AccessDescriptor>> ALL_PROPERTIES_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> METHOD_CACHE = new ConcurrentHashMap<>();

    private static Map<String, AccessDescriptor> buildDescriptorMap(Class<?> type) {
        var fields = type.getFields();
        var map = new LinkedHashMap<String, AccessDescriptor>(fields.length);
        for (var f : fields) {
            f.setAccessible(true);
            try {
                var getter = LOOKUP.unreflectGetter(f);
                map.put(f.getName(), new AccessDescriptor(f.getName(), getter));
            } catch (IllegalAccessException ignored) {
                // skip forbidden fields
            }
        }
        for (var m : type.getMethods()) {
            var methodName = m.getName();
            if (m.getParameterCount() == 0 && (methodName.startsWith("get") || methodName.startsWith("is"))) {
                var name = propertyNameFromGetter(methodName);
                m.setAccessible(true);
                try {
                    var mh = LOOKUP.unreflect(m);
                    map.put(name, new AccessDescriptor(name, mh));
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

    private static String propertyNameFromGetter(String methodName) {
        if (methodName.startsWith("get")) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is")) {
            return decapitalize(methodName.substring(2));
        }
        return methodName;
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

    private static ConversionResult tryConvertArgs(Class<?>[] params, List<Object> args) {
        var converted = new Object[params.length];
        int score = 0;

        for (int i = 0; i < params.length; i++) {
            Object arg = i < args.size() ? args.get(i) : null;
            if (arg == null) {
                converted[i] = null;
                continue;
            }

            var paramType = wrap(params[i]);
            var argType = wrap(arg.getClass());

            if (paramType.equals(argType)) {
                converted[i] = arg;
            } else if (paramType.isEnum() && arg instanceof String) {
                converted[i] = Enum.valueOf((Class<Enum>) paramType, (String) arg);
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

    @AllArgsConstructor
    private static class AccessDescriptor {
        private String name;
        private MethodHandle getter;

        Object get(Object target) throws Throwable {
            return getter == null ? null : getter.invoke(target);
        }
    }

    private static class ConversionResult {
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

        if (obj instanceof Map<?, ?>) {
            var map = (Map<?, ?>) obj;
            return map.get(name);
        }

        var type = obj.getClass();
        if (isInt(name)) {
            var idx = Integer.parseInt(name);
            if (obj.getClass().isArray()) {
                var len = Array.getLength(obj);
                if (idx < 0 || idx >= len) {
                    throw new TemplateEvalException("Array index out of range: " + idx);
                }
                return Array.get(obj, idx);
            }
            if (obj instanceof List<?>) {
                var list = (List<?>) obj;
                if (idx < 0 || idx >= list.size()) {
                    throw new TemplateEvalException("List index out of range: " + idx);
                }
                return list.get(idx);
            }
            if (obj instanceof CharSequence) {
                var seq = (CharSequence) obj;
                if (idx < 0 || idx >= seq.length()) {
                    throw new TemplateEvalException("String index out of range: " + idx);
                }
                return Character.toString(seq.charAt(idx));
            }
        }

        var map = PROPERTY_CACHE.computeIfAbsent(type, c -> new ConcurrentHashMap<>());
        var desc = map.computeIfAbsent(name, n -> buildDescriptor(type, n));
        try {
            return desc.get(obj);
        } catch (Throwable e) {
            throw new TemplateEvalException("Failed to access property '" + name + "' of " + type, e);
        }
    }

    public static Object invokeMethodReflective(Object target, String methodName, List<Object> args) {
        if (target == null) {
            throw new TemplateEvalException("Cannot call method on null target");
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
            throw new TemplateEvalException("No matching method " + methodName + " found for args " + args);
        }

        if (bestMatch.isVarArgs()) {
            var paramTypes = bestMatch.getParameterTypes();
            int normalCount = paramTypes.length - 1;
            Object[] newArgs = new Object[paramTypes.length];
            System.arraycopy(bestConverted, 0, newArgs, 0, normalCount);

            var varargType = paramTypes[normalCount].getComponentType();
            Object varargArray = Array.newInstance(varargType, args.size() - normalCount);

            for (int i = normalCount; i < args.size(); i++) {
                Array.set(varargArray, i - normalCount, args.get(i));
            }
            newArgs[normalCount] = varargArray;
            bestConverted = newArgs;
        }

        try {
            bestMatch.setAccessible(true);
            return bestMatch.invoke(target, bestConverted);
        } catch (ReflectiveOperationException e) {
            throw new TemplateEvalException("Error invoking method " + methodName + ": " + e.getMessage(), e);
        }
    }
}

