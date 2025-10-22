package io.github.sibmaks.jjtemplate.evaluator.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class ReflectionUtils {
    private static String decapitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Get all public fields of class
     *
     * @param type type to scan
     * @return public fields
     */
    public static Map<String, Field> scanFields(Class<?> type) {
        var map = new HashMap<String, Field>();
        for (var f : type.getFields()) {
            f.setAccessible(true);
            map.put(f.getName(), f);
        }
        return map;
    }

    /**
     * Get all public methods of class
     *
     * @param type type to scan
     * @return public methods
     */
    public static Map<String, Method> scanMethods(Class<?> type) {
        var map = new HashMap<String, Method>();
        for (var m : type.getMethods()) {
            if (m.getParameterCount() != 0) {
                continue;
            }
            var name = m.getName();
            if (name.startsWith("get") && name.length() > 3) {
                map.put(decapitalize(name.substring(3)), m);
            } else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                map.put(decapitalize(name.substring(2)), m);
            }
        }
        return map;
    }
}
