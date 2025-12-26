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
    void testVisitSwitch() {
        var visitor = new StubVisitor();
        var expression = new SwitchExpression(null, null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.SWITCH, actual);
    }

    @Test
    void testVisitRange() {
        var visitor = new StubVisitor();
        var expression = new RangeExpression(null, "item", "index", null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.RANGE, actual);
    }

    @Test
    void testVisitThenSwitchCase() {
        var visitor = new StubVisitor();
        var expression = new ThenSwitchCaseExpression(null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.THEN_SWITCH, actual);
    }

    @Test
    void testVisitElseSwitchCase() {
        var visitor = new StubVisitor();
        var expression = new ElseSwitchCaseExpression(null);
        var actual = expression.accept(visitor);
        assertEquals(ExpressionType.ELSE_SWITCH, actual);
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
        TERNARY,
        SWITCH,
        RANGE,
        THEN_SWITCH,
        ELSE_SWITCH
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

        @Override
        public ExpressionType visitSwitch(SwitchExpression expr) {
            return ExpressionType.SWITCH;
        }

        @Override
        public ExpressionType visitRange(RangeExpression expr) {
            return ExpressionType.RANGE;
        }

        @Override
        public ExpressionType visitThenSwitchCase(ThenSwitchCaseExpression expr) {
            return ExpressionType.THEN_SWITCH;
        }

        @Override
        public ExpressionType visitElseSwitchCase(ElseSwitchCaseExpression expr) {
            return ExpressionType.ELSE_SWITCH;
        }
    }
}
