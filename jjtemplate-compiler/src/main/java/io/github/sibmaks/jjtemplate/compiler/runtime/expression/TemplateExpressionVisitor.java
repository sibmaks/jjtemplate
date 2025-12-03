package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ElseTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;

/**
 * Visitor interface for traversing and transforming template expressions.
 * <p>
 * Each concrete {@link io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression}
 * delegates to this visitor, allowing evaluation, rewriting, type inference
 * or analysis of expression structures.
 * </p>
 *
 * @param <R> the type returned by each visit operation
 * @author sibmaks
 * @since 0.5.0
 */
public interface TemplateExpressionVisitor<R> {
    /**
     * Visits a function call expression and performs a visitor-defined operation.
     *
     * @param expression the function call expression to visit
     * @return visitor-defined result
     */
    R visit(FunctionCallTemplateExpression expression);

    /**
     * Visits a pipe-chain expression consisting of a root expression and a sequence
     * of function calls applied through the pipe operator.
     *
     * @param expression the pipe-chain expression to visit
     * @return visitor-defined result
     */
    R visit(PipeChainTemplateExpression expression);

    /**
     * Visits a concatenation expression representing a sequence of template
     * fragments combined into a single value.
     *
     * @param expression the concatenation expression to visit
     * @return visitor-defined result
     */
    R visit(TemplateConcatTemplateExpression expression);

    /**
     * Visits a ternary expression composed of a condition, a value for the true
     * branch, and a value for the false branch.
     *
     * @param expression the ternary expression to visit
     * @return visitor-defined result
     */
    R visit(TernaryTemplateExpression expression);

    /**
     * Visits a value expression that holds a constant literal.
     *
     * @param expression the literal value expression to visit
     * @return visitor-defined result
     */
    R visit(ConstantTemplateExpression expression);

    /**
     * Visits a variable reference expression.
     *
     * @param expression the variable reference expression to visit
     * @return visitor-defined result
     */
    R visit(VariableTemplateExpression expression);

    /**
     * Visits an object template expression.
     *
     * @param expression object template expression
     * @return visitor-defined result
     */
    R visit(ObjectTemplateExpression expression);

    /**
     * Visits a switch template expression.
     *
     * @param expression switch template expression
     * @return visitor-defined result
     */
    R visit(SwitchTemplateExpression expression);

    /**
     * Visits a list template expression.
     *
     * @param expression list template expression
     * @return visitor-defined result
     */
    R visit(ListTemplateExpression expression);

    /**
     * Visits a range template expression.
     *
     * @param expression range template expression
     * @return visitor-defined result
     */
    R visit(RangeTemplateExpression expression);

    /**
     * Visits an {@code else} template expression.
     *
     * @param expression else template expression
     * @return visitor-defined result
     */
    R visit(ElseTemplateExpression expression);

}
