package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sibmaks
 * @since 0.9.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReflectionPropertySupport {
    private static final List<String> GET_METHOD_PREFIX = List.of("get", "is");
    private static final Object PROPERTY_NOT_FOUND = new Object();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, Map<String, AccessDescriptor>> PROPERTY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<AccessDescriptor>> ALL_PROPERTIES_CACHE = new ConcurrentHashMap<>();

    static Map<String, Object> getAllProperties(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        if (obj instanceof Map<?, ?>) {
            return getMapProperties((Map<?, ?>) obj);
        }
        if (obj instanceof List<?>) {
            return getListProperties((List<?>) obj);
        }

        var type = obj.getClass();
        var descriptors = ALL_PROPERTIES_CACHE.computeIfAbsent(type, ReflectionPropertySupport::buildAllDescriptors);
        var map = new LinkedHashMap<String, Object>(descriptors.size());
        for (var descriptor : descriptors) {
            try {
                map.put(descriptor.name, descriptor.get(obj));
            } catch (Throwable ignored) {
                // ignore unavailable fields
            }
        }
        return map;
    }

    static Object getProperty(Object obj, String name) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof FieldResolver) {
            var resolver = (FieldResolver) obj;
            return resolver.resolve(name);
        }

        if (obj instanceof Map<?, ?>) {
            return getMapProperty(obj, (Map<?, ?>) obj, name);
        }

        var indexedProperty = getIndexedProperty(obj, name);
        if (indexedProperty != PROPERTY_NOT_FOUND) {
            return indexedProperty;
        }

        try {
            return getObjectProperty(obj, name, obj.getClass());
        } catch (TemplateEvalException exception) {
            var unknownPropertyMessage = "Unknown property '" + name + "' of " + obj.getClass();
            if (!unknownPropertyMessage.equals(exception.getMessage())) {
                throw exception;
            }
            if (obj instanceof FieldFallbackResolver) {
                var fallbackResolver = (FieldFallbackResolver) obj;
                return fallbackResolver.resolve(name);
            }
            throw exception;
        }
    }

    static Object getProperty(
            Object obj,
            String name,
            List<ReflectionUtils.ResolvedProperty> properties
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
            } catch (Throwable exception) {
                throw new TemplateEvalException("Failed to access property '" + name + "' of " + obj.getClass(), exception);
            }
        }
        return getProperty(obj, name);
    }

    static Optional<ReflectionUtils.ResolvedProperty> resolveProperty(Class<?> type, String propertyName) {
        if (type == null || type.isArray() || Map.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)) {
            return Optional.empty();
        }
        var descriptors = buildDescriptorMap(type);
        var descriptor = descriptors.get(propertyName);
        if (descriptor == null) {
            return Optional.empty();
        }
        return Optional.of(new ReflectionUtils.ResolvedProperty(type, propertyName, descriptor.getter, descriptor.valueType));
    }

    private static Map<String, Object> getMapProperties(Map<?, ?> source) {
        var result = new LinkedHashMap<String, Object>();
        for (var entry : source.entrySet()) {
            if (entry.getKey() instanceof String) {
                result.put((String) entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static Map<String, Object> getListProperties(List<?> source) {
        var result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < source.size(); i++) {
            result.put(String.valueOf(i), source.get(i));
        }
        return result;
    }

    private static Object getMapProperty(Object source, Map<?, ?> map, String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        if (source instanceof FieldFallbackResolver) {
            var fallbackResolver = (FieldFallbackResolver) source;
            return fallbackResolver.resolve(name);
        }
        return null;
    }

    private static Object getIndexedProperty(Object obj, String name) {
        if (!isInt(name)) {
            return PROPERTY_NOT_FOUND;
        }
        var idx = Integer.parseInt(name);
        if (obj.getClass().isArray()) {
            return getArrayItem(obj, idx);
        }
        if (obj instanceof List<?>) {
            return getListItem((List<?>) obj, idx);
        }
        if (obj instanceof CharSequence) {
            return getStringChar((CharSequence) obj, idx);
        }
        return PROPERTY_NOT_FOUND;
    }

    private static Object getObjectProperty(Object obj, String name, Class<?> type) {
        var map = PROPERTY_CACHE.computeIfAbsent(type, key -> new ConcurrentHashMap<>());
        var descriptor = map.computeIfAbsent(name, propertyName -> buildDescriptor(type, propertyName));
        try {
            return descriptor.get(obj);
        } catch (Throwable exception) {
            throw new TemplateEvalException("Failed to access property '" + name + "' of " + type, exception);
        }
    }

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
            if (method.getParameterCount() > 0) {
                continue;
            }
            var methodName = method.getName();
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
        var descriptor = buildDescriptorMap(type).get(name);
        if (descriptor == null) {
            throw new TemplateEvalException(String.format("Unknown property '%s' of %s", name, type));
        }
        return descriptor;
    }

    private static List<AccessDescriptor> buildAllDescriptors(Class<?> type) {
        return new ArrayList<>(buildDescriptorMap(type).values());
    }

    private static String decapitalize(String value) {
        return value.isEmpty() ? value : Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static boolean isInt(String value) {
        for (var i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
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

    private static Object getArrayItem(Object array, int idx) {
        var len = Array.getLength(array);
        if (idx < 0 || idx >= len) {
            throw new TemplateEvalException("Array index out of range: " + idx);
        }
        return Array.get(array, idx);
    }

    @AllArgsConstructor
    private static final class AccessDescriptor {
        private final String name;
        private final MethodHandle getter;
        private final Class<?> valueType;

        Object get(Object target) throws Throwable {
            return getter == null ? null : getter.invoke(target);
        }
    }
}
