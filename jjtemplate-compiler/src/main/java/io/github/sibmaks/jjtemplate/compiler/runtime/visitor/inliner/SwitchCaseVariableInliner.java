package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.*;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class SwitchCaseVariableInliner implements SwitchCaseVisitor<SwitchCase> {
    private final TemplateExpressionVariableInliner inliner;

    @Override
    public SwitchCase visit(ExpressionSwitchCase expressionSwitchCase) {
        var key = expressionSwitchCase.getKey();
        var inlinedKey = key.visit(inliner);
        var anyInlined = key != inlinedKey;

        var value = expressionSwitchCase.getValue();
        var inlinedValue = value.visit(inliner);
        anyInlined |= value != inlinedValue;

        if (anyInlined) {
            return new ExpressionSwitchCase(inlinedKey, inlinedValue);
        }

        return expressionSwitchCase;
    }

    @Override
    public SwitchCase visit(ConstantSwitchCase constantSwitchCase) {
        var key = constantSwitchCase.getConstant();

        var value = constantSwitchCase.getValue();
        var inlinedValue = value.visit(inliner);
        var anyInlined = value != inlinedValue;

        if (anyInlined) {
            return new ConstantSwitchCase(key, inlinedValue);
        }

        return constantSwitchCase;
    }

    @Override
    public SwitchCase visit(ElseTemplateExpression elseTemplateExpression) {
        var value = elseTemplateExpression.getValue();
        var inlinedValue = value.visit(inliner);
        var anyInlined = value != inlinedValue;

        if (anyInlined) {
            return new ElseTemplateExpression(inlinedValue);
        }

        return elseTemplateExpression;
    }
}
