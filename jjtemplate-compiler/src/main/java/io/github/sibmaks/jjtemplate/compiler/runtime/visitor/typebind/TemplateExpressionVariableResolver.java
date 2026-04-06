package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateTypeValidationMode;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class TemplateExpressionVariableResolver {
    private final TemplateTypeValidationMode validationMode;

    TemplateExpressionVariableResolver(TemplateCompileContext compileContext) {
        this.validationMode = compileContext.validationMode();
    }

    TemplateExpression bindVariableExpression(
            VariableTemplateExpression expression,
            CompileScope scope,
            TemplateExpressionBindingEngine bindingEngine
    ) {
        var preparedChains = bindVariableArguments(expression, scope, bindingEngine);
        var rootTypes = scope.lookup(expression.getRootName());
        var chainsChanged = preparedChains != expression.getCallChain();
        if (rootTypes.isEmpty() || !rootTypes.get().isKnown()) {
            if (!chainsChanged) {
                return expression;
            }
            return new VariableTemplateExpression(
                    expression.getRootName(),
                    preparedChains,
                    expression.getSourceExpression()
            );
        }

        var currentTypes = rootTypes.get();
        var boundChains = new ArrayList<VariableTemplateExpression.Chain>(preparedChains.size());
        var changed = chainsChanged;
        for (int i = 0; i < preparedChains.size(); i++) {
            var chain = preparedChains.get(i);
            var resolution = resolveChain(currentTypes, chain, scope, expression.getDiagnosticExpression(), bindingEngine);
            if (resolution.status() == ResolutionStatus.INVALID) {
                throw new TemplateCompilationException(resolution.message());
            }
            boundChains.add(resolution.chain() == null ? chain : resolution.chain());
            if (resolution.status() == ResolutionStatus.UNKNOWN) {
                for (int j = i + 1; j < preparedChains.size(); j++) {
                    boundChains.add(preparedChains.get(j));
                }
                return new VariableTemplateExpression(
                        expression.getRootName(),
                        boundChains,
                        expression.getSourceExpression()
                );
            }
            currentTypes = resolution.nextTypes();
            changed = true;
        }

        if (!changed) {
            return expression;
        }
        return new VariableTemplateExpression(expression.getRootName(), boundChains, expression.getSourceExpression());
    }

    CompileTypeSet inferVariableType(
            VariableTemplateExpression expression,
            CompileScope scope,
            TemplateExpressionBindingEngine bindingEngine
    ) {
        var types = scope.lookup(expression.getRootName());
        if (types.isEmpty() || !types.get().isKnown()) {
            return CompileTypeSet.unknown();
        }
        var current = types.get();
        for (var chain : expression.getCallChain()) {
            var resolution = resolveChain(current, chain, scope, expression.getDiagnosticExpression(), bindingEngine);
            if (resolution.status() != ResolutionStatus.RESOLVED) {
                return CompileTypeSet.unknown();
            }
            current = resolution.nextTypes();
        }
        return current;
    }

    private List<VariableTemplateExpression.Chain> bindVariableArguments(
            VariableTemplateExpression expression,
            CompileScope scope,
            TemplateExpressionBindingEngine bindingEngine
    ) {
        var originalChain = expression.getCallChain();
        var preparedChains = new ArrayList<VariableTemplateExpression.Chain>(originalChain.size());
        var changed = false;
        for (var chain : originalChain) {
            if (chain instanceof VariableTemplateExpression.CallMethodChain) {
                var call = (VariableTemplateExpression.CallMethodChain) chain;
                var args = new ArrayList<TemplateExpression>(call.getArgsExpressions().size());
                var argsChanged = false;
                for (var arg : call.getArgsExpressions()) {
                    var bound = bindingEngine.bindExpression(arg, scope);
                    argsChanged |= bound != arg;
                    args.add(bound);
                }
                if (argsChanged) {
                    preparedChains.add(new VariableTemplateExpression.CallMethodChain(call.getMethodName(), args));
                    changed = true;
                    continue;
                }
            }
            preparedChains.add(chain);
        }
        if (!changed) {
            return originalChain;
        }
        return preparedChains;
    }

    private ChainResolution resolveChain(
            CompileTypeSet currentTypes,
            VariableTemplateExpression.Chain chain,
            CompileScope scope,
            String expressionText,
            TemplateExpressionBindingEngine bindingEngine
    ) {
        if (chain instanceof VariableTemplateExpression.GetPropertyChain) {
            var property = (VariableTemplateExpression.GetPropertyChain) chain;
            return resolveProperty(currentTypes, property.getPropertyName(), expressionText);
        }
        if (chain instanceof VariableTemplateExpression.CallMethodChain) {
            var method = (VariableTemplateExpression.CallMethodChain) chain;
            return resolveMethod(currentTypes, method, scope, expressionText, bindingEngine);
        }
        if (chain instanceof VariableTemplateExpression.BoundPropertyChain
                || chain instanceof VariableTemplateExpression.BoundMethodChain) {
            return new ChainResolution(ResolutionStatus.RESOLVED, currentTypes, chain, null);
        }
        return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), chain, null);
    }

    private ChainResolution resolveProperty(
            CompileTypeSet currentTypes,
            String propertyName,
            String expressionText
    ) {
        var resolvedProperties = new ArrayList<ReflectionUtils.ResolvedProperty>(currentTypes.types().size());
        var nextTypes = new LinkedHashSet<Class<?>>();
        var hasUnknown = false;
        var hasInvalid = false;
        for (var type : currentTypes.types()) {
            if (Map.class.isAssignableFrom(type)) {
                hasUnknown = true;
                continue;
            }
            if (type.isArray()) {
                if (isNumericSegment(propertyName)) {
                    nextTypes.add(type.getComponentType());
                    continue;
                }
                hasInvalid = true;
                continue;
            }
            if (List.class.isAssignableFrom(type)) {
                if (isNumericSegment(propertyName)) {
                    hasUnknown = true;
                    continue;
                }
                hasInvalid = true;
                continue;
            }
            if (CharSequence.class.isAssignableFrom(type) && isNumericSegment(propertyName)) {
                nextTypes.add(String.class);
                continue;
            }

            var resolved = ReflectionUtils.resolveProperty(type, propertyName);
            if (resolved.isPresent()) {
                resolvedProperties.add(resolved.get());
                nextTypes.add(resolved.get().getValueType());
                continue;
            }
            if (validationMode == TemplateTypeValidationMode.SOFT && ReflectionUtils.isSoftlyExtensible(type)) {
                hasUnknown = true;
            } else {
                hasInvalid = true;
            }
        }

        if (validationMode == TemplateTypeValidationMode.STRICT && hasInvalid) {
            return invalidProperty(expressionText, propertyName, currentTypes.types());
        }
        if (hasUnknown) {
            return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), null, null);
        }
        if (hasInvalid) {
            return invalidProperty(expressionText, propertyName, currentTypes.types());
        }
        return new ChainResolution(
                ResolutionStatus.RESOLVED,
                CompileTypeSet.known(nextTypes),
                resolvedProperties.isEmpty()
                        ? null
                        : new VariableTemplateExpression.BoundPropertyChain(propertyName, resolvedProperties),
                null
        );
    }

    private ChainResolution resolveMethod(
            CompileTypeSet currentTypes,
            VariableTemplateExpression.CallMethodChain method,
            CompileScope scope,
            String expressionText,
            TemplateExpressionBindingEngine bindingEngine
    ) {
        var argTypes = new ArrayList<Class<?>>(method.getArgsExpressions().size());
        for (var arg : method.getArgsExpressions()) {
            var inferredType = bindingEngine.inferExpressionType(arg, scope);
            argTypes.add(inferredType.isKnown() && inferredType.types().size() == 1 ? inferredType.types().get(0) : null);
        }

        var nextTypes = new LinkedHashSet<Class<?>>();
        var resolvedMethods = new ArrayList<ReflectionUtils.ResolvedMethod>(currentTypes.types().size());
        var hasUnknown = false;
        var hasInvalid = false;
        for (var type : currentTypes.types()) {
            var methods = ReflectionUtils.resolveMethods(type, method.getMethodName(), argTypes);
            if (methods.size() == 1) {
                var resolvedMethod = methods.get(0);
                resolvedMethods.add(resolvedMethod);
                nextTypes.add(resolvedMethod.getReturnType());
                continue;
            }
            if (methods.size() > 1) {
                hasUnknown = true;
                continue;
            }
            if (validationMode == TemplateTypeValidationMode.SOFT && ReflectionUtils.isSoftlyExtensible(type)) {
                hasUnknown = true;
            } else {
                hasInvalid = true;
            }
        }

        if (validationMode == TemplateTypeValidationMode.STRICT && (hasUnknown || hasInvalid)) {
            return invalidMethod(expressionText, method.getMethodName(), currentTypes.types());
        }
        if (hasUnknown) {
            return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), null, null);
        }
        if (hasInvalid) {
            return invalidMethod(expressionText, method.getMethodName(), currentTypes.types());
        }
        return new ChainResolution(
                ResolutionStatus.RESOLVED,
                CompileTypeSet.known(nextTypes),
                resolvedMethods.isEmpty()
                        ? null
                        : new VariableTemplateExpression.BoundMethodChain(
                                method.getMethodName(),
                                method.getArgsExpressions(),
                                resolvedMethods
                        ),
                null
        );
    }

    private ChainResolution invalidProperty(
            String expressionText,
            String propertyName,
            List<Class<?>> types
    ) {
        return new ChainResolution(
                ResolutionStatus.INVALID,
                CompileTypeSet.unknown(),
                null,
                String.format("Unknown property '%s' in expression '%s' for types %s", propertyName, expressionText, renderTypes(types))
        );
    }

    private ChainResolution invalidMethod(
            String expressionText,
            String methodName,
            List<Class<?>> types
    ) {
        return new ChainResolution(
                ResolutionStatus.INVALID,
                CompileTypeSet.unknown(),
                null,
                String.format("Unknown method '%s' in expression '%s' for types %s", methodName, expressionText, renderTypes(types))
        );
    }

    private String renderTypes(List<Class<?>> types) {
        var names = new ArrayList<String>(types.size());
        for (var type : types) {
            names.add(type.getName());
        }
        return names.toString();
    }

    private boolean isNumericSegment(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return !name.isEmpty();
    }
}
