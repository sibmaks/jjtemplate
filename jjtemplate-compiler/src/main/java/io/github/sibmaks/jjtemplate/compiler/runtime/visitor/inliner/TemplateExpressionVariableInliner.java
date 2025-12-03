package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ElseTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;

import java.util.ArrayList;
import java.util.Map;

/**
 * Performs variable inlining inside template expressions.
 * <p>
 * This visitor replaces occurrences of {@link VariableTemplateExpression}
 * with their constant values when such values are known at compile time.
 * <p>
 * Complex expressions (function calls, pipe chains, ternaries, concatenations)
 * are recursively traversed, and only the modified parts are rebuilt.
 *
 * <p>
 * Inlining happens only when:
 * <ul>
 *   <li>a variable exists in the provided value map,</li>
 *   <li>all function-call arguments in chained accesses inline into static values,</li>
 *   <li>property/method chains can be evaluated immediately.</li>
 * </ul>
 * If any part of a chain cannot be resolved statically, the original expression is preserved.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateExpressionVariableInliner implements TemplateExpressionVisitor<TemplateExpression> {
    private final Map<String, Object> values;
    private final ObjectElementVariableInliner objectElementVariableInliner;
    private final ListElementVariableInliner listElementVariableInliner;
    private final SwitchCaseVariableInliner switchCaseVariableInliner;

    public TemplateExpressionVariableInliner(Map<String, Object> values) {
        this.values = values;
        this.objectElementVariableInliner = new ObjectElementVariableInliner(this);
        this.listElementVariableInliner = new ListElementVariableInliner(this);
        this.switchCaseVariableInliner = new SwitchCaseVariableInliner(this);
    }

    @Override
    public TemplateExpression visit(FunctionCallTemplateExpression function) {
        var args = new ArrayList<TemplateExpression>();
        var anyInlined = false;
        for (var argExpression : function.getArgExpressions()) {
            var inlined = argExpression.visit(this);
            args.add(inlined);
            if (argExpression != inlined) {
                anyInlined = true;
            }
        }
        if (!anyInlined) {
            return function;
        }
        return new FunctionCallTemplateExpression(function.getFunction(), args);
    }

    @Override
    public TemplateExpression visit(PipeChainTemplateExpression pipe) {
        var root = pipe.getRoot();
        var inlinedRoot = root.visit(this);
        var chain = new ArrayList<FunctionCallTemplateExpression>();
        var anyChainInlined = false;
        for (var pipeChainFunction : pipe.getChain()) {
            var args = new ArrayList<TemplateExpression>();
            var anyInlined = false;
            for (var argExpression : pipeChainFunction.getArgExpressions()) {
                var inlined = argExpression.visit(this);
                args.add(inlined);
                if (argExpression != inlined) {
                    anyInlined = true;
                }
            }
            if (!anyInlined) {
                chain.add(pipeChainFunction);
            } else {
                anyChainInlined = true;
                chain.add(new FunctionCallTemplateExpression(
                        pipeChainFunction.getFunction(),
                        args
                ));
            }
        }

        if (root == inlinedRoot && !anyChainInlined) {
            return pipe;
        }
        return new PipeChainTemplateExpression(inlinedRoot, chain);
    }

    @Override
    public TemplateExpression visit(TemplateConcatTemplateExpression template) {
        var expressions = new ArrayList<TemplateExpression>();
        var anyInlined = false;
        for (var expression : template.getExpressions()) {
            var inlined = expression.visit(this);
            expressions.add(inlined);
            if (expression != inlined) {
                anyInlined = true;
            }
        }
        if (!anyInlined) {
            return template;
        }
        return new TemplateConcatTemplateExpression(expressions);
    }

    @Override
    public TemplateExpression visit(TernaryTemplateExpression ternary) {
        var condition = ternary.getCondition();
        var inlinedCondition = condition.visit(this);

        var thenTrue = ternary.getThenTrue();
        var inlinedThenTrue = thenTrue.visit(this);

        var thenFalse = ternary.getThenFalse();
        var inlinedThenFalse = thenFalse.visit(this);

        if (condition != inlinedCondition ||
                thenTrue != inlinedThenTrue ||
                thenFalse != inlinedThenFalse) {
            return new TernaryTemplateExpression(inlinedCondition, inlinedThenTrue, inlinedThenFalse);
        }
        return ternary;
    }

    @Override
    public TemplateExpression visit(ConstantTemplateExpression expression) {
        return expression;
    }

    @Override
    public TemplateExpression visit(VariableTemplateExpression variable) {
        var rootName = variable.getRootName();
        var value = values.get(rootName);
        if (value == null && !values.containsKey(rootName)) {
            return variable;
        }
        if (value == null) {
            return new ConstantTemplateExpression(null);
        }

        var callChain = variable.getCallChain();
        for (var chain : callChain) {
            if (chain instanceof VariableTemplateExpression.CallMethodChain) {
                var callMethodChain = (VariableTemplateExpression.CallMethodChain) chain;
                for (var argsExpression : callMethodChain.getArgsExpressions()) {
                    var inlined = argsExpression.visit(this);
                    if (!(inlined instanceof ConstantTemplateExpression)) {
                        return inlined;
                    }
                }
                value = callMethodChain.apply(Context.empty(), value);
            } else if (chain instanceof VariableTemplateExpression.GetPropertyChain) {
                var propertyChain = (VariableTemplateExpression.GetPropertyChain) chain;
                value = propertyChain.apply(Context.empty(), value);
            } else {
                return variable;
            }
        }

        return new ConstantTemplateExpression(value);
    }

    @Override
    public TemplateExpression visit(ListTemplateExpression expression) {
        var elements = expression.getElements();
        var inlinedElements = new ArrayList<ListElement>(elements.size());
        var anyInlined = false;
        for (var element : elements) {
            var inlinedElement = element.visit(listElementVariableInliner);
            var inlined = inlinedElement != element;
            anyInlined |= inlined;
            inlinedElements.add(inlinedElement);
        }
        if (!anyInlined) {
            return expression;
        }
        return new ListTemplateExpression(inlinedElements);
    }

    @Override
    public TemplateExpression visit(RangeTemplateExpression expression) {
        var source = expression.getSource();
        var inlinedSource = source.visit(this);
        var anyInlined = source != inlinedSource;

        var body = expression.getBody();
        var inlinedBody = body.visit(this);
        anyInlined |= body != inlinedBody;

        var name = expression.getName();
        var inlinedName = name.visit(this);
        anyInlined |= name != inlinedName;

        if (anyInlined) {
            return RangeTemplateExpression.builder()
                    .name(inlinedName)
                    .indexVariableName(expression.getIndexVariableName())
                    .itemVariableName(expression.getItemVariableName())
                    .source(inlinedSource)
                    .body(inlinedBody)
                    .build();
        }

        return expression;
    }

    @Override
    public TemplateExpression visit(ElseTemplateExpression expression) {
        var value = expression.getValue();
        var inlined = value.visit(this);
        if (value != inlined) {
            return new ElseTemplateExpression(inlined);
        }
        return expression;
    }

    @Override
    public TemplateExpression visit(ObjectTemplateExpression expression) {
        var elements = expression.getElements();
        var inlinedElements = new ArrayList<ObjectElement>(elements.size());
        var anyInlined = false;
        for (var element : elements) {
            var inlinedElement = element.visit(objectElementVariableInliner);
            var inlined = inlinedElement != element;
            anyInlined |= inlined;
            inlinedElements.add(inlinedElement);
        }
        if (!anyInlined) {
            return expression;
        }
        return new ObjectTemplateExpression(inlinedElements);
    }

    @Override
    public TemplateExpression visit(SwitchTemplateExpression expression) {
        var switchKey = expression.getSwitchKey();
        var inlinedSwitchKey = switchKey.visit(this);
        var anyInlined = switchKey != inlinedSwitchKey;

        var condition = expression.getCondition();
        var inlinedCondition = condition.visit(this);
        anyInlined |= condition != inlinedCondition;

        var cases = expression.getCases();
        var inlinedCases = new ArrayList<SwitchCase>(cases.size());

        for (var switchCase : cases) {
            var inlinedCase = switchCase.visit(switchCaseVariableInliner);
            anyInlined |= switchCase != inlinedCase;
            inlinedCases.add(inlinedCase);
        }

        if (anyInlined) {
            return SwitchTemplateExpression.builder()
                    .switchKey(inlinedSwitchKey)
                    .condition(inlinedCondition)
                    .cases(inlinedCases)
                    .build();
        }

        return expression;
    }
}
