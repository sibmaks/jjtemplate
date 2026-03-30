package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateTypeValidationMode;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.FunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ConditionListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.DynamicListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.SpreadListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.SpreadObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ConstantSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ElseSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ExpressionSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Performs compile-time validation and partial binding of typed variable access chains.
 *
 * @author sibmaks
 * @since 0.9.0
 */
public final class TemplateExpressionTypeBinder {
    private final TemplateCompileContext compileContext;
    private final TemplateTypeValidationMode validationMode;

    /**
     * Creates a binder for the provided compile-time context.
     *
     * @param compileContext compile-time type source
     */
    public TemplateExpressionTypeBinder(TemplateCompileContext compileContext) {
        this.compileContext = compileContext;
        this.validationMode = compileContext.validationMode();
    }

    /**
     * Applies compile-time binding and validation to a compiled template tree.
     *
     * @param compiledTemplate compiled template
     * @return bound template
     */
    public CompiledTemplateImpl bind(CompiledTemplateImpl compiledTemplate) {
        var scope = new CompileScope(null, Map.of(), compileContext);
        var boundVariables = new ArrayList<ObjectFieldElement>(compiledTemplate.getInternalVariables().size());
        for (var variable : compiledTemplate.getInternalVariables()) {
            var boundKey = bindExpression(variable.getKey(), scope);
            var boundValue = bindExpression(variable.getValue(), scope);
            boundVariables.add(new ObjectFieldElement(boundKey, boundValue));

            if (boundKey instanceof ConstantTemplateExpression && boundValue != null) {
                var keyValue = ((ConstantTemplateExpression) boundKey).getValue();
                if (keyValue instanceof String) {
                    var inferredType = inferExpressionType(boundValue, scope);
                    if (inferredType.isKnown()) {
                        scope = scope.child(Map.of((String) keyValue, inferredType));
                    }
                }
            }
        }

        var boundTemplate = bindExpression(compiledTemplate.getCompiledTemplate(), scope);
        return new CompiledTemplateImpl(boundVariables, boundTemplate);
    }

    private TemplateExpression bindExpression(TemplateExpression expression, CompileScope scope) {
        if (expression instanceof ConstantTemplateExpression) {
            return expression;
        }
        if (expression instanceof VariableTemplateExpression) {
            return bindVariableExpression((VariableTemplateExpression) expression, scope);
        }
        if (expression instanceof TemplateConcatTemplateExpression) {
            var concat = (TemplateConcatTemplateExpression) expression;
            var expressions = concat.getExpressions();
            var boundExpressions = new ArrayList<TemplateExpression>(expressions.size());
            var changed = false;
            for (var item : expressions) {
                var bound = bindExpression(item, scope);
                changed |= bound != item;
                boundExpressions.add(bound);
            }
            if (!changed) {
                return expression;
            }
            return new TemplateConcatTemplateExpression(boundExpressions);
        }
        if (expression instanceof TernaryTemplateExpression) {
            var ternary = (TernaryTemplateExpression) expression;
            var condition = bindExpression(ternary.getCondition(), scope);
            var thenTrue = bindExpression(ternary.getThenTrue(), scope);
            var thenFalse = bindExpression(ternary.getThenFalse(), scope);
            if (condition == ternary.getCondition() &&
                    thenTrue == ternary.getThenTrue() &&
                    thenFalse == ternary.getThenFalse()) {
                return expression;
            }
            return new TernaryTemplateExpression(condition, thenTrue, thenFalse, ternary.getSourceExpression());
        }
        if (expression instanceof PipeChainTemplateExpression) {
            var pipe = (PipeChainTemplateExpression) expression;
            var root = bindExpression(pipe.getRoot(), scope);
            var chain = new ArrayList<FunctionCallTemplateExpression>(pipe.getChain().size());
            var changed = root != pipe.getRoot();
            for (var item : pipe.getChain()) {
                var bound = bindFunction(item, scope);
                changed |= bound != item;
                chain.add(bound);
            }
            if (!changed) {
                return expression;
            }
            return new PipeChainTemplateExpression(root, chain, pipe.getSourceExpression());
        }
        if (expression instanceof DynamicFunctionCallTemplateExpression) {
            var function = (DynamicFunctionCallTemplateExpression) expression;
            var args = bindListTemplate(function.getArgExpression(), scope);
            if (args == function.getArgExpression()) {
                return expression;
            }
            return new DynamicFunctionCallTemplateExpression(function.getFunction(), args, function.getSourceExpression());
        }
        if (expression instanceof ConstantFunctionCallTemplateExpression) {
            return expression;
        }
        if (expression instanceof ListTemplateExpression) {
            return bindListTemplate((ListTemplateExpression) expression, scope);
        }
        if (expression instanceof ObjectTemplateExpression) {
            return bindObjectTemplate((ObjectTemplateExpression) expression, scope);
        }
        if (expression instanceof RangeTemplateExpression) {
            var range = (RangeTemplateExpression) expression;
            var name = bindExpression(range.getName(), scope);
            var source = bindExpression(range.getSource(), scope);
            var itemType = inferRangeItemType(inferExpressionType(source, scope));
            var childScope = scope.child(Map.of(
                    range.getIndexVariableName(), CompileTypeSet.known(Integer.class),
                    range.getItemVariableName(), itemType
            ));
            var body = bindExpression(range.getBody(), childScope);
            if (name == range.getName() && source == range.getSource() && body == range.getBody()) {
                return expression;
            }
            return RangeTemplateExpression.builder()
                    .name(name)
                    .itemVariableName(range.getItemVariableName())
                    .indexVariableName(range.getIndexVariableName())
                    .source(source)
                    .body(body)
                    .sourceExpression(range.getSourceExpression())
                    .build();
        }
        if (expression instanceof SwitchTemplateExpression) {
            var switchExpression = (SwitchTemplateExpression) expression;
            var condition = bindExpression(switchExpression.getCondition(), scope);
            var cases = new ArrayList<SwitchCase>(switchExpression.getCases().size());
            var changed = condition != switchExpression.getCondition();
            for (var switchCase : switchExpression.getCases()) {
                var bound = bindSwitchCase(switchCase, scope);
                changed |= bound != switchCase;
                cases.add(bound);
            }
            if (!changed) {
                return expression;
            }
            return SwitchTemplateExpression.builder()
                    .condition(condition)
                    .cases(cases)
                    .sourceExpression(switchExpression.getSourceExpression())
                    .build();
        }
        return expression;
    }

    private FunctionCallTemplateExpression bindFunction(FunctionCallTemplateExpression expression, CompileScope scope) {
        if (expression instanceof DynamicFunctionCallTemplateExpression) {
            var function = (DynamicFunctionCallTemplateExpression) expression;
            var args = bindListTemplate(function.getArgExpression(), scope);
            if (args == function.getArgExpression()) {
                return expression;
            }
            return new DynamicFunctionCallTemplateExpression(function.getFunction(), args, function.getSourceExpression());
        }
        return expression;
    }

    private ListTemplateExpression bindListTemplate(ListTemplateExpression expression, CompileScope scope) {
        var elements = expression.getElements();
        var boundElements = new ArrayList<ListElement>(elements.size());
        var changed = false;
        for (var element : elements) {
            var bound = bindListElement(element, scope);
            changed |= bound != element;
            boundElements.add(bound);
        }
        if (!changed) {
            return expression;
        }
        return new ListTemplateExpression(boundElements);
    }

    private ListElement bindListElement(ListElement element, CompileScope scope) {
        if (element instanceof DynamicListElement) {
            var dynamic = (DynamicListElement) element;
            var value = bindExpression(dynamic.getValue(), scope);
            if (value == dynamic.getValue()) {
                return element;
            }
            return new DynamicListElement(value);
        }
        if (element instanceof ConditionListElement) {
            var condition = (ConditionListElement) element;
            var source = bindExpression(condition.getSource(), scope);
            if (source == condition.getSource()) {
                return element;
            }
            return new ConditionListElement(source);
        }
        if (element instanceof SpreadListElement) {
            var spread = (SpreadListElement) element;
            var source = bindExpression(spread.getSource(), scope);
            if (source == spread.getSource()) {
                return element;
            }
            return new SpreadListElement(source);
        }
        return element;
    }

    private ObjectTemplateExpression bindObjectTemplate(ObjectTemplateExpression expression, CompileScope scope) {
        var elements = expression.getElements();
        var boundElements = new ArrayList<ObjectElement>(elements.size());
        var changed = false;
        for (var element : elements) {
            var bound = bindObjectElement(element, scope);
            changed |= bound != element;
            boundElements.add(bound);
        }
        if (!changed) {
            return expression;
        }
        return new ObjectTemplateExpression(boundElements);
    }

    private ObjectElement bindObjectElement(ObjectElement element, CompileScope scope) {
        if (element instanceof ObjectFieldElement) {
            var field = (ObjectFieldElement) element;
            var key = bindExpression(field.getKey(), scope);
            var value = bindExpression(field.getValue(), scope);
            if (key == field.getKey() && value == field.getValue()) {
                return element;
            }
            return new ObjectFieldElement(key, value);
        }
        if (element instanceof SpreadObjectElement) {
            var spread = (SpreadObjectElement) element;
            var source = bindExpression(spread.getSource(), scope);
            if (source == spread.getSource()) {
                return element;
            }
            return new SpreadObjectElement(source);
        }
        return element;
    }

    private SwitchCase bindSwitchCase(SwitchCase switchCase, CompileScope scope) {
        if (switchCase instanceof ConstantSwitchCase) {
            var constant = (ConstantSwitchCase) switchCase;
            var value = bindExpression(constant.getValue(), scope);
            if (value == constant.getValue()) {
                return switchCase;
            }
            return new ConstantSwitchCase(constant.getConstant(), value);
        }
        if (switchCase instanceof ElseSwitchCase) {
            var elseCase = (ElseSwitchCase) switchCase;
            var value = bindExpression(elseCase.getValue(), scope);
            if (value == elseCase.getValue()) {
                return switchCase;
            }
            return ElseSwitchCase.builder().value(value).build();
        }
        if (switchCase instanceof ExpressionSwitchCase) {
            var expressionCase = (ExpressionSwitchCase) switchCase;
            var key = bindExpression(expressionCase.getKey(), scope);
            var value = bindExpression(expressionCase.getValue(), scope);
            if (key == expressionCase.getKey() && value == expressionCase.getValue()) {
                return switchCase;
            }
            return new ExpressionSwitchCase(key, value);
        }
        return switchCase;
    }

    private TemplateExpression bindVariableExpression(
            VariableTemplateExpression expression,
            CompileScope scope
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
                    var bound = bindExpression(arg, scope);
                    argsChanged |= bound != arg;
                    args.add(bound);
                }
                if (argsChanged) {
                    preparedChains.add(new VariableTemplateExpression.CallMethodChain(call.getMethodName(), args));
                    changed = true;
                } else {
                    preparedChains.add(chain);
                }
            } else {
                preparedChains.add(chain);
            }
        }

        var rootTypes = scope.lookup(expression.getRootName());
        if (rootTypes.isEmpty() || !rootTypes.get().isKnown()) {
            if (!changed) {
                return expression;
            }
            return new VariableTemplateExpression(expression.getRootName(), preparedChains, expression.getSourceExpression());
        }

        var currentTypes = rootTypes.get();
        var boundChains = new ArrayList<VariableTemplateExpression.Chain>(preparedChains.size());
        for (int i = 0; i < preparedChains.size(); i++) {
            var chain = preparedChains.get(i);
            var resolution = resolveChain(currentTypes, chain, scope, expression.getDiagnosticExpression());
            if (resolution.status == ResolutionStatus.INVALID) {
                throw new TemplateCompilationException(resolution.message);
            }
            boundChains.add(resolution.chain == null ? chain : resolution.chain);
            if (resolution.status == ResolutionStatus.UNKNOWN) {
                for (int j = i + 1; j < preparedChains.size(); j++) {
                    boundChains.add(preparedChains.get(j));
                }
                return new VariableTemplateExpression(expression.getRootName(), boundChains, expression.getSourceExpression());
            }
            currentTypes = resolution.nextTypes;
            changed = true;
        }

        if (!changed) {
            return expression;
        }
        return new VariableTemplateExpression(expression.getRootName(), boundChains, expression.getSourceExpression());
    }

    private ChainResolution resolveChain(
            CompileTypeSet currentTypes,
            VariableTemplateExpression.Chain chain,
            CompileScope scope,
            String expressionText
    ) {
        if (chain instanceof VariableTemplateExpression.GetPropertyChain) {
            var property = (VariableTemplateExpression.GetPropertyChain) chain;
            return resolveProperty(currentTypes, property.getPropertyName(), expressionText);
        }
        if (chain instanceof VariableTemplateExpression.CallMethodChain) {
            var method = (VariableTemplateExpression.CallMethodChain) chain;
            return resolveMethod(currentTypes, method, scope, expressionText);
        }
        if (chain instanceof VariableTemplateExpression.BoundPropertyChain) {
            return new ChainResolution(ResolutionStatus.RESOLVED, currentTypes, chain, null);
        }
        if (chain instanceof VariableTemplateExpression.BoundMethodChain) {
            return new ChainResolution(ResolutionStatus.RESOLVED, currentTypes, chain, null);
        }
        return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), chain, null);
    }

    private ChainResolution resolveProperty(
            CompileTypeSet currentTypes,
            String propertyName,
            String expressionText
    ) {
        var resolvedProperties = new ArrayList<ReflectionUtils.ResolvedProperty>(currentTypes.types.size());
        var nextTypes = new LinkedHashSet<Class<?>>();
        var hasUnknown = false;
        var hasInvalid = false;
        for (var type : currentTypes.types) {
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
            return invalidProperty(expressionText, propertyName, currentTypes.types);
        }
        if (hasUnknown) {
            return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), null, null);
        }
        if (hasInvalid) {
            return invalidProperty(expressionText, propertyName, currentTypes.types);
        }
        return new ChainResolution(
                ResolutionStatus.RESOLVED,
                CompileTypeSet.known(nextTypes),
                resolvedProperties.isEmpty() ? null : new VariableTemplateExpression.BoundPropertyChain(propertyName, resolvedProperties),
                null
        );
    }

    private ChainResolution resolveMethod(
            CompileTypeSet currentTypes,
            VariableTemplateExpression.CallMethodChain method,
            CompileScope scope,
            String expressionText
    ) {
        var argTypes = new ArrayList<Class<?>>(method.getArgsExpressions().size());
        for (var arg : method.getArgsExpressions()) {
            var inferredType = inferExpressionType(arg, scope);
            argTypes.add(inferredType.isKnown() && inferredType.types.size() == 1 ? inferredType.types.get(0) : null);
        }

        var nextTypes = new LinkedHashSet<Class<?>>();
        var resolvedMethods = new ArrayList<ReflectionUtils.ResolvedMethod>(currentTypes.types.size());
        var hasUnknown = false;
        var hasInvalid = false;
        for (var type : currentTypes.types) {
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
            return invalidMethod(expressionText, method.getMethodName(), currentTypes.types);
        }
        if (hasUnknown) {
            return new ChainResolution(ResolutionStatus.UNKNOWN, CompileTypeSet.unknown(), null, null);
        }
        if (hasInvalid) {
            return invalidMethod(expressionText, method.getMethodName(), currentTypes.types);
        }
        return new ChainResolution(
                ResolutionStatus.RESOLVED,
                CompileTypeSet.known(nextTypes),
                resolvedMethods.isEmpty() ? null : new VariableTemplateExpression.BoundMethodChain(
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

    private CompileTypeSet inferRangeItemType(CompileTypeSet sourceType) {
        if (!sourceType.isKnown()) {
            return CompileTypeSet.unknown();
        }
        var itemTypes = new LinkedHashSet<Class<?>>();
        for (var type : sourceType.types) {
            if (!type.isArray()) {
                return CompileTypeSet.unknown();
            }
            itemTypes.add(type.getComponentType());
        }
        return CompileTypeSet.known(itemTypes);
    }

    private CompileTypeSet inferExpressionType(TemplateExpression expression, CompileScope scope) {
        if (expression instanceof ConstantTemplateExpression) {
            var value = ((ConstantTemplateExpression) expression).getValue();
            if (value == null) {
                return CompileTypeSet.unknown();
            }
            return CompileTypeSet.known(value.getClass());
        }
        if (expression instanceof VariableTemplateExpression) {
            return inferVariableType((VariableTemplateExpression) expression, scope);
        }
        if (expression instanceof ListTemplateExpression || expression instanceof RangeTemplateExpression) {
            return CompileTypeSet.known(List.class);
        }
        if (expression instanceof ObjectTemplateExpression) {
            return CompileTypeSet.known(Map.class);
        }
        if (expression instanceof TernaryTemplateExpression) {
            var ternary = (TernaryTemplateExpression) expression;
            var thenType = inferExpressionType(ternary.getThenTrue(), scope);
            var elseType = inferExpressionType(ternary.getThenFalse(), scope);
            if (!thenType.isKnown() || !elseType.isKnown()) {
                return CompileTypeSet.unknown();
            }
            var merged = new LinkedHashSet<Class<?>>();
            merged.addAll(thenType.types);
            merged.addAll(elseType.types);
            return CompileTypeSet.known(merged);
        }
        return CompileTypeSet.unknown();
    }

    private CompileTypeSet inferVariableType(VariableTemplateExpression expression, CompileScope scope) {
        var types = scope.lookup(expression.getRootName());
        if (types.isEmpty() || !types.get().isKnown()) {
            return CompileTypeSet.unknown();
        }
        var current = types.get();
        for (var chain : expression.getCallChain()) {
            var resolution = resolveChain(current, chain, scope, expression.getDiagnosticExpression());
            if (resolution.status != ResolutionStatus.RESOLVED) {
                return CompileTypeSet.unknown();
            }
            current = resolution.nextTypes;
        }
        return current;
    }

    private enum ResolutionStatus {
        RESOLVED,
        UNKNOWN,
        INVALID
    }

    private static final class ChainResolution {
        private final ResolutionStatus status;
        private final CompileTypeSet nextTypes;
        private final VariableTemplateExpression.Chain chain;
        private final String message;

        private ChainResolution(
                ResolutionStatus status,
                CompileTypeSet nextTypes,
                VariableTemplateExpression.Chain chain,
                String message
        ) {
            this.status = status;
            this.nextTypes = nextTypes;
            this.chain = chain;
            this.message = message;
        }
    }

    private static final class CompileTypeSet {
        private final boolean known;
        private final List<Class<?>> types;

        private CompileTypeSet(boolean known, List<Class<?>> types) {
            this.known = known;
            this.types = types;
        }

        static CompileTypeSet unknown() {
            return new CompileTypeSet(false, List.of());
        }

        static CompileTypeSet known(Class<?> type) {
            return new CompileTypeSet(true, List.of(type));
        }

        static CompileTypeSet known(Iterable<Class<?>> types) {
            var unique = new LinkedHashSet<Class<?>>();
            for (var type : types) {
                if (type != null) {
                    unique.add(type);
                }
            }
            if (unique.isEmpty()) {
                return unknown();
            }
            return new CompileTypeSet(true, List.copyOf(unique));
        }

        boolean isKnown() {
            return known;
        }
    }

    private static final class CompileScope {
        private final CompileScope parent;
        private final Map<String, CompileTypeSet> types;
        private final TemplateCompileContext compileContext;

        private CompileScope(
                CompileScope parent,
                Map<String, CompileTypeSet> types,
                TemplateCompileContext compileContext
        ) {
            this.parent = parent;
            this.types = types;
            this.compileContext = compileContext;
        }

        CompileScope child(Map<String, CompileTypeSet> local) {
            return new CompileScope(this, new LinkedHashMap<>(local), compileContext);
        }

        Optional<CompileTypeSet> lookup(String variableName) {
            if (types.containsKey(variableName)) {
                return Optional.of(types.get(variableName));
            }
            if (parent != null) {
                return parent.lookup(variableName);
            }
            var fromContext = compileContext.lookupTypes(variableName);
            return fromContext.map(CompileTypeSet::known);
        }
    }
}
