package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.9.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReflectionConversionSupport {

    static ConversionResult tryConvertArgs(Class<?>[] params, List<Object> args) {
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

    static Object[] convertVarArgs(
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
        return newArgs;
    }

    static boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class;
    }

    static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        switch (type.getName()) {
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
                return type;
        }
    }

    static Object resolveEnumConstant(Class<?> enumType, String enumValue) {
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

    private static Object convertNumber(Number number, Class<?> targetType) {
        if (targetType == Integer.class || targetType == int.class) {
            return number.intValue();
        }
        if (targetType == Long.class || targetType == long.class) {
            return number.longValue();
        }
        if (targetType == Double.class || targetType == double.class) {
            return number.doubleValue();
        }
        if (targetType == Float.class || targetType == float.class) {
            return number.floatValue();
        }
        if (targetType == Short.class || targetType == short.class) {
            return number.shortValue();
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return number.byteValue();
        }
        return number;
    }

    static final class ConversionResult {
        private final Object[] values;
        private final int score;

        ConversionResult(Object[] values, int score) {
            this.values = values;
            this.score = score;
        }

        Object[] getValues() {
            return values;
        }

        int getScore() {
            return score;
        }
    }
}
