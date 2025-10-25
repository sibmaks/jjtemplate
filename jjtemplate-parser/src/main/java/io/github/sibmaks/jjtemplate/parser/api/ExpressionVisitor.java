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

}
