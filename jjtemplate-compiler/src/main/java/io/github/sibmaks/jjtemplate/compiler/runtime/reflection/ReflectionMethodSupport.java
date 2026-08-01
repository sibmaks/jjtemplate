package io.github.sibmaks.jjtemplate.compiler.runtime.reflection;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.9.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReflectionMethodSupport {
    private static final ClassValue<Method[]> METHOD_CACHE = new ClassValue<>() {
        @Override
        protected Method[] computeValue(Class<?> type) {
            return type.getMethods();
        }
    };

    static Object invokeMethodReflective(Object target, String methodName, List<Object> args) {
        if (target == null) {
            throw new TemplateEvalException("Cannot call method on null target");
        }

        if (target instanceof MethodResolver) {
            var resolver = (MethodResolver) target;
            return resolver.resolve(methodName, args.toArray());
        }

        var type = target.getClass();
        var methods = METHOD_CACHE.get(type);
        var bestMatch = findBestMatch(methods, methodName, args);

        if (bestMatch == null) {
            if (target instanceof MethodFallbackResolver) {
                var fallbackResolver = (MethodFallbackResolver) target;
                return fallbackResolver.resolve(methodName, args.toArray());
            }
            throw new TemplateEvalException("No matching method " + methodName + " found for args " + args);
        }

        var method = bestMatch.method;
        var converted = bestMatch.converted;
        if (method.isVarArgs()) {
            converted = ReflectionConversionSupport.convertVarArgs(args, method, converted);
        }

        try {
            method.setAccessible(true);
            return method.invoke(target, converted);
        } catch (ReflectiveOperationException exception) {
            throw ReflectionUtils.methodInvocationError(methodName, exception);
        }
    }

    private static MethodMatch findBestMatch(Method[] methods, String methodName, List<Object> args) {
        MethodMatch bestMatch = null;
        for (var method : methods) {
            var candidate = matchMethod(method, methodName, args);
            if (candidate != null && (bestMatch == null || candidate.score < bestMatch.score)) {
                bestMatch = candidate;
            }
        }
        return bestMatch;
    }

    private static MethodMatch matchMethod(Method method, String methodName, List<Object> args) {
        if (!method.getName().equals(methodName) || !acceptsArgumentCount(method, args.size())) {
            return null;
        }
        var params = method.getParameterTypes();
        var argsToConvert = prepareArguments(method, args);
        var conversion = ReflectionConversionSupport.tryConvertArgs(params, argsToConvert);
        if (conversion == null) {
            return null;
        }
        return new MethodMatch(method, conversion.getValues(), conversion.getScore());
    }

    private static boolean acceptsArgumentCount(Method method, int argumentCount) {
        var parameterCount = method.getParameterCount();
        return method.isVarArgs() ? argumentCount >= parameterCount - 1 : parameterCount == argumentCount;
    }

    private static List<Object> prepareArguments(Method method, List<Object> args) {
        if (!method.isVarArgs()) {
            return args;
        }
        var params = method.getParameterTypes();
        return mergeVarArgs(args, params);
    }

    static Object invokeMethodReflective(
            Object target,
            String methodName,
            List<Object> args,
            List<ReflectionUtils.ResolvedMethod> resolvedMethods
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
            } catch (ReflectiveOperationException exception) {
                throw ReflectionUtils.methodInvocationError(methodName, exception);
            }
        }
        return invokeMethodReflective(target, methodName, args);
    }

    static List<ReflectionUtils.ResolvedMethod> resolveMethods(
            Class<?> type,
            String methodName,
            List<Class<?>> argTypes
    ) {
        var methods = METHOD_CACHE.get(type);
        var result = new ArrayList<ReflectionUtils.ResolvedMethod>();
        for (var method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (!isMethodCompatible(method, argTypes)) {
                continue;
            }
            result.add(new ReflectionUtils.ResolvedMethod(type, method));
        }
        return result;
    }

    static Object invokeResolvedMethod(Method method, Object target, List<Object> args)
            throws ReflectiveOperationException {
        var params = method.getParameterTypes();
        Object[] converted;
        if (method.isVarArgs()) {
            var merged = mergeVarArgs(args, params);
            var conversion = ReflectionConversionSupport.tryConvertArgs(params, merged);
            if (conversion == null) {
                throw new TemplateEvalException("No matching method " + method.getName() + " found for args " + args);
            }
            converted = ReflectionConversionSupport.convertVarArgs(args, method, conversion.getValues());
        } else {
            var conversion = ReflectionConversionSupport.tryConvertArgs(params, args);
            if (conversion == null) {
                throw new TemplateEvalException("No matching method " + method.getName() + " found for args " + args);
            }
            converted = conversion.getValues();
        }
        method.setAccessible(true);
        return method.invoke(target, converted);
    }

    private static List<Object> mergeVarArgs(List<Object> args, Class<?>[] params) {
        var fixedCount = params.length - 1;
        var varargType = params[fixedCount].getComponentType();
        var varargArray = Array.newInstance(varargType, args.size() - fixedCount);
        for (int i = fixedCount; i < args.size(); i++) {
            Array.set(varargArray, i - fixedCount, args.get(i));
        }
        var merged = new ArrayList<>(args.subList(0, fixedCount));
        merged.add(varargArray);
        return merged;
    }

    private static boolean isMethodCompatible(Method method, List<Class<?>> argTypes) {
        var params = method.getParameterTypes();
        if (!method.isVarArgs()) {
            if (params.length != argTypes.size()) {
                return false;
            }
            for (int i = 0; i < params.length; i++) {
                if (!isArgumentCompatible(params[i], argTypes.get(i))) {
                    return false;
                }
            }
            return true;
        }

        var fixedCount = params.length - 1;
        if (argTypes.size() < fixedCount) {
            return false;
        }
        for (int i = 0; i < fixedCount; i++) {
            if (!isArgumentCompatible(params[i], argTypes.get(i))) {
                return false;
            }
        }
        var varArgType = ReflectionConversionSupport.wrap(params[fixedCount].getComponentType());
        for (int i = fixedCount; i < argTypes.size(); i++) {
            if (!isWrappedArgumentCompatible(varArgType, argTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isArgumentCompatible(Class<?> parameterType, Class<?> argType) {
        if (argType == null) {
            return true;
        }
        return isWrappedArgumentCompatible(ReflectionConversionSupport.wrap(parameterType), argType);
    }

    private static boolean isWrappedArgumentCompatible(Class<?> wrappedParameterType, Class<?> argType) {
        var wrappedArgType = ReflectionConversionSupport.wrap(argType);
        return wrappedParameterType.equals(wrappedArgType)
                || wrappedParameterType.isAssignableFrom(wrappedArgType)
                || ReflectionConversionSupport.isNumeric(wrappedParameterType)
                && ReflectionConversionSupport.isNumeric(wrappedArgType);
    }

    private static final class MethodMatch {
        private final Method method;
        private final Object[] converted;
        private final int score;

        private MethodMatch(Method method, Object[] converted, int score) {
            this.method = method;
            this.converted = converted;
            this.score = score;
        }
    }
}
