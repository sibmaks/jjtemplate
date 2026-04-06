package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.PipeChainTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.RangeTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateConcatTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TernaryTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;
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
import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class TemplateExpressionBindingEngine {
    private final TemplateCompileContext compileContext;
    private final TemplateExpressionVariableResolver variableResolver;

    TemplateExpressionBindingEngine(TemplateCompileContext compileContext) {
        this.compileContext = compileContext;
        this.variableResolver = new TemplateExpressionVariableResolver(compileContext);
    }

    CompiledTemplateImpl bind(CompiledTemplateImpl compiledTemplate) {
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

    TemplateExpression bindExpression(TemplateExpression expression, CompileScope scope) {
        if (expression instanceof ConstantTemplateExpression) {
            return expression;
        }
        if (expression instanceof VariableTemplateExpression) {
            return variableResolver.bindVariableExpression((VariableTemplateExpression) expression, scope, this);
        }
        if (expression instanceof TemplateConcatTemplateExpression) {
            return bindConcatExpression((TemplateConcatTemplateExpression) expression, scope, expression);
        }
        if (expression instanceof TernaryTemplateExpression) {
            return bindTernaryExpression((TernaryTemplateExpression) expression, scope, expression);
        }
        if (expression instanceof PipeChainTemplateExpression) {
            return bindPipeExpression((PipeChainTemplateExpression) expression, scope, expression);
        }
        if (expression instanceof DynamicFunctionCallTemplateExpression) {
            return bindDynamicFunctionExpression((DynamicFunctionCallTemplateExpression) expression, scope, expression);
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
            return bindRangeExpression((RangeTemplateExpression) expression, scope, expression);
        }
        if (expression instanceof SwitchTemplateExpression) {
            return bindSwitchExpression((SwitchTemplateExpression) expression, scope, expression);
        }
        return expression;
    }

    CompileTypeSet inferExpressionType(TemplateExpression expression, CompileScope scope) {
        if (expression instanceof ConstantTemplateExpression) {
            var value = ((ConstantTemplateExpression) expression).getValue();
            if (value == null) {
                return CompileTypeSet.unknown();
            }
            return CompileTypeSet.known(value.getClass());
        }
        if (expression instanceof VariableTemplateExpression) {
            return variableResolver.inferVariableType((VariableTemplateExpression) expression, scope, this);
        }
        if (expression instanceof ListTemplateExpression || expression instanceof RangeTemplateExpression) {
            return CompileTypeSet.known(List.class);
        }
        if (expression instanceof ObjectTemplateExpression) {
            return CompileTypeSet.known(Map.class);
        }
        if (expression instanceof TernaryTemplateExpression) {
            return inferTernaryType((TernaryTemplateExpression) expression, scope);
        }
        return CompileTypeSet.unknown();
    }

    CompileTypeSet inferRangeItemType(CompileTypeSet sourceType) {
        if (!sourceType.isKnown()) {
            return CompileTypeSet.unknown();
        }
        var itemTypes = new LinkedHashSet<Class<?>>();
        for (var type : sourceType.types()) {
            if (!type.isArray()) {
                return CompileTypeSet.unknown();
            }
            itemTypes.add(type.getComponentType());
        }
        return CompileTypeSet.known(itemTypes);
    }

    private TemplateExpression bindConcatExpression(
            TemplateConcatTemplateExpression concat,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var expressions = concat.getExpressions();
        var boundExpressions = new ArrayList<TemplateExpression>(expressions.size());
        var changed = false;
        for (var item : expressions) {
            var bound = bindExpression(item, scope);
            changed |= bound != item;
            boundExpressions.add(bound);
        }
        if (!changed) {
            return originalExpression;
        }
        return new TemplateConcatTemplateExpression(boundExpressions);
    }

    private TemplateExpression bindTernaryExpression(
            TernaryTemplateExpression ternary,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var condition = bindExpression(ternary.getCondition(), scope);
        var thenTrue = bindExpression(ternary.getThenTrue(), scope);
        var thenFalse = bindExpression(ternary.getThenFalse(), scope);
        if (condition == ternary.getCondition()
                && thenTrue == ternary.getThenTrue()
                && thenFalse == ternary.getThenFalse()) {
            return originalExpression;
        }
        return new TernaryTemplateExpression(condition, thenTrue, thenFalse, ternary.getSourceExpression());
    }

    private TemplateExpression bindPipeExpression(
            PipeChainTemplateExpression pipe,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var root = bindExpression(pipe.getRoot(), scope);
        var chain = new ArrayList<FunctionCallTemplateExpression>(pipe.getChain().size());
        var changed = root != pipe.getRoot();
        for (var item : pipe.getChain()) {
            var bound = bindFunction(item, scope);
            changed |= bound != item;
            chain.add(bound);
        }
        if (!changed) {
            return originalExpression;
        }
        return new PipeChainTemplateExpression(root, chain, pipe.getSourceExpression());
    }

    private TemplateExpression bindDynamicFunctionExpression(
            DynamicFunctionCallTemplateExpression function,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var args = bindListTemplate(function.getArgExpression(), scope);
        if (args == function.getArgExpression()) {
            return originalExpression;
        }
        return new DynamicFunctionCallTemplateExpression(function.getFunction(), args, function.getSourceExpression());
    }

    private TemplateExpression bindRangeExpression(
            RangeTemplateExpression range,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var name = bindExpression(range.getName(), scope);
        var source = bindExpression(range.getSource(), scope);
        var itemType = inferRangeItemType(inferExpressionType(source, scope));
        var childScope = scope.child(Map.of(
                range.getIndexVariableName(), CompileTypeSet.known(Integer.class),
                range.getItemVariableName(), itemType
        ));
        var body = bindExpression(range.getBody(), childScope);
        if (name == range.getName() && source == range.getSource() && body == range.getBody()) {
            return originalExpression;
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

    private TemplateExpression bindSwitchExpression(
            SwitchTemplateExpression switchExpression,
            CompileScope scope,
            TemplateExpression originalExpression
    ) {
        var condition = bindExpression(switchExpression.getCondition(), scope);
        var cases = new ArrayList<SwitchCase>(switchExpression.getCases().size());
        var changed = condition != switchExpression.getCondition();
        for (var switchCase : switchExpression.getCases()) {
            var bound = bindSwitchCase(switchCase, scope);
            changed |= bound != switchCase;
            cases.add(bound);
        }
        if (!changed) {
            return originalExpression;
        }
        return SwitchTemplateExpression.builder()
                .condition(condition)
                .cases(cases)
                .sourceExpression(switchExpression.getSourceExpression())
                .build();
    }

    private CompileTypeSet inferTernaryType(TernaryTemplateExpression ternary, CompileScope scope) {
        var thenType = inferExpressionType(ternary.getThenTrue(), scope);
        var elseType = inferExpressionType(ternary.getThenFalse(), scope);
        if (!thenType.isKnown() || !elseType.isKnown()) {
            return CompileTypeSet.unknown();
        }
        var merged = new LinkedHashSet<Class<?>>();
        merged.addAll(thenType.types());
        merged.addAll(elseType.types());
        return CompileTypeSet.known(merged);
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
}
