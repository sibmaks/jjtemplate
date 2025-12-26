package io.github.sibmaks.jjtemplate.parser.api;

/**
 * Visitor interface for traversing and transforming expression trees.
 *
 * @param <R> result type produced by the visitor
 * @author sibmaks
 * @since 0.0.1
 */
public interface ExpressionVisitor<R> {
    /**
     * Visits a {@link LiteralExpression}.
     *
     * @param expr the literal expression to visit
     * @return the result of visiting the literal expression
     */
    R visitLiteral(LiteralExpression expr);

    /**
     * Visits a {@link VariableExpression}.
     *
     * @param expr the variable expression to visit
     * @return the result of visiting the variable expression
     */
    R visitVariable(VariableExpression expr);

    /**
     * Visits a {@link FunctionCallExpression}.
     *
     * @param expr the function call expression to visit
     * @return the result of visiting the function call expression
     */
    R visitFunction(FunctionCallExpression expr);

    /**
     * Visits a {@link PipeExpression}.
     *
     * @param expr the pipe expression to visit
     * @return the result of visiting the pipe expression
     */
    R visitPipe(PipeExpression expr);

    /**
     * Visits a {@link TernaryExpression}.
     *
     * @param expr the ternary expression to visit
     * @return the result of visiting the ternary expression
     */
    R visitTernary(TernaryExpression expr);

    /**
     * Visits a {@link SwitchExpression}.
     *
     * @param expr the switch expression to visit
     * @return the result of visiting the switch expression
     */
    R visitSwitch(SwitchExpression expr);

    /**
     * Visits a {@link RangeExpression}.
     *
     * @param expr the range expression to visit
     * @return the result of visiting the range expression
     */
    R visitRange(RangeExpression expr);

    /**
     * Visits a {@link ThenSwitchCaseExpression}.
     *
     * @param expr the then-case switch expression to visit
     * @return the result of visiting the then-case switch expression
     */
    R visitThenSwitchCase(ThenSwitchCaseExpression expr);

    /**
     * Visits a {@link ElseSwitchCaseExpression}.
     *
     * @param expr the else-case switch expression to visit
     * @return the result of visiting the else-case switch expression
     */
    R visitElseSwitchCase(ElseSwitchCaseExpression expr);

}
