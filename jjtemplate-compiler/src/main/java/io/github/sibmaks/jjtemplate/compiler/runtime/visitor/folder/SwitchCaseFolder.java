package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.*;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class SwitchCaseFolder implements SwitchCaseVisitor<SwitchCase> {
    private final TemplateExpressionFolder folder;

    @Override
    public SwitchCase visit(ExpressionSwitchCase expressionSwitchCase) {
        var key = expressionSwitchCase.getKey();
        var foldedKey = key.visit(folder);
        var anyFolded = key != foldedKey;

        var value = expressionSwitchCase.getValue();
        var foldedValue = value.visit(folder);
        anyFolded |= value != foldedValue;

        if (foldedKey instanceof ConstantTemplateExpression) {
            var constantTemplateExpression = (ConstantTemplateExpression) foldedKey;
            var keyValue = constantTemplateExpression.getValue();
            return new ConstantSwitchCase(keyValue, foldedValue);
        }

        if (anyFolded) {
            return new ExpressionSwitchCase(foldedKey, foldedValue);
        }

        return expressionSwitchCase;
    }

    @Override
    public SwitchCase visit(ConstantSwitchCase constantSwitchCase) {
        var key = constantSwitchCase.getConstant();

        var value = constantSwitchCase.getValue();
        var foldedValue = value.visit(folder);
        var anyFolded = value != foldedValue;

        if (anyFolded) {
            return new ConstantSwitchCase(key, foldedValue);
        }

        return constantSwitchCase;
    }

    @Override
    public SwitchCase visit(ElseSwitchCase elseSwitchCase) {
        var value = elseSwitchCase.getValue();
        var foldedValue = value.visit(folder);
        var anyFolded = value != foldedValue;

        if (anyFolded) {
            return new ElseSwitchCase(foldedValue);
        }

        return elseSwitchCase;
    }
}
