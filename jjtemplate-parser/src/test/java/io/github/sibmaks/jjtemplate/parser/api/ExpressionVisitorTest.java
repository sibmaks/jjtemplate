package io.github.sibmaks.jjtemplate.parser.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author sibmaks
 */
class ExpressionVisitorTest {
    @Test
    void testVisitFunctionCallExpression() {
        var visitor = new StubVisitor();
        var expression = new FunctionCallExpression(UUID.randomUUID().toString(), List.of());
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.FUNCTION, actual);
    }

    @Test
    void testVisitLiteral() {
        var visitor = new StubVisitor();
        var expression = new LiteralExpression("dd.MM.yyyy'T'HH:mm:ss");
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.LITERAL, actual);
    }

    @Test
    void testVisitPipe() {
        var visitor = new StubVisitor();
        var expression = new PipeExpression(null, null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.PIPE, actual);
    }

    @Test
    void testVisitTernary() {
        var visitor = new StubVisitor();
        var expression = new TernaryExpression(null, null, null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.TERNARY, actual);
    }

    @Test
    void testVisitVariable() {
        var visitor = new StubVisitor();
        var expression = new VariableExpression(List.of());
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.VARIABLE, actual);
    }

    enum ExpressionType {
        LITERAL,
        VARIABLE,
        FUNCTION,
        PIPE,
        TERNARY
    }

    static class StubVisitor implements ExpressionVisitor<ExpressionType> {

        @Override
        public ExpressionType visitLiteral(LiteralExpression expr) {
            return ExpressionType.LITERAL;
        }

        @Override
        public ExpressionType visitVariable(VariableExpression expr) {
            return ExpressionType.VARIABLE;
        }

        @Override
        public ExpressionType visitFunction(FunctionCallExpression expr) {
            return ExpressionType.FUNCTION;
        }

        @Override
        public ExpressionType visitPipe(PipeExpression expr) {
            return ExpressionType.PIPE;
        }

        @Override
        public ExpressionType visitTernary(TernaryExpression expr) {
            return ExpressionType.TERNARY;
        }
    }
}