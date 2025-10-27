package io.github.sibmaks.jjtemplate.evaluator.reflection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
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
            throw new IllegalArgumentException(String.format("Unknown property '%s' of %s", name, type));
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

    @AllArgsConstructor
    private static class AccessDescriptor {
        private String name;
        private MethodHandle getter;

        Object get(Object target) throws Throwable {
            return getter == null ? null : getter.invoke(target);
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
                    throw new IllegalArgumentException("Array index out of range: " + idx);
                }
                return Array.get(obj, idx);
            }
            if (obj instanceof List<?>) {
                var list = (List<?>) obj;
                if (idx < 0 || idx >= list.size()) {
                    throw new IllegalArgumentException("List index out of range: " + idx);
                }
                return list.get(idx);
            }
            if (obj instanceof CharSequence) {
                var seq = (CharSequence) obj;
                if (idx < 0 || idx >= seq.length()) {
                    throw new IllegalArgumentException("String index out of range: " + idx);
                }
                return Character.toString(seq.charAt(idx));
            }
        }

        var map = PROPERTY_CACHE.computeIfAbsent(type, c -> new ConcurrentHashMap<>());
        var desc = map.computeIfAbsent(name, n -> buildDescriptor(type, n));
        try {
            return desc.get(obj);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to access property '" + name + "' of " + type, e);
        }
    }
}

