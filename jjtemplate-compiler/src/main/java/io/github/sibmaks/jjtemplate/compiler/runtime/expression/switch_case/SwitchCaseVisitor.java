package io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case;

/**
 * Visitor interface for processing switch-case elements.
 * <p>
 * Used to traverse or transform {@link SwitchCase} implementations without
 * coupling logic to concrete case types.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface SwitchCaseVisitor<T> {
    /**
     * Visits a switch case defined by a dynamic key expression.
     *
     * @param expressionSwitchCase expression-based switch case
     * @return visitor-defined result
     */
    T visit(ExpressionSwitchCase expressionSwitchCase);

    /**
     * Visits a switch case defined by a constant value.
     *
     * @param constantSwitchCase constant-based switch case
     * @return visitor-defined result
     */
    T visit(ConstantSwitchCase constantSwitchCase);

    /**
     * Visits an unconditional {@code else} switch case.
     *
     * @param elseTemplateExpression else switch case
     * @return visitor-defined result
     */
    T visit(ElseTemplateExpression elseTemplateExpression);
}
