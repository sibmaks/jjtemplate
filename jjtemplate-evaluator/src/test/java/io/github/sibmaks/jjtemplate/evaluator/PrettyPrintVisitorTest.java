package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.parser.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class PrettyPrintVisitorTest {
    @InjectMocks
    private PrettyPrintVisitor visitor;

    @Test
    void testLiteral_string() {
        var expr = new LiteralExpression("hello");
        assertEquals("'hello'", visitor.visitLiteral(expr));
    }

    @Test
    void testLiteral_number() {
        var expr = new LiteralExpression(123);
        assertEquals("123", visitor.visitLiteral(expr));
    }

    @Test
    void testVariable_simple() {
        var segment = new VariableExpression.Segment("name");
        var expr = new VariableExpression(List.of(segment));

        assertEquals(".name", visitor.visitVariable(expr));
    }

    @Test
    void testVariable_withMethod() {
        var arg1 = new LiteralExpression(10);
        var arg2 = new LiteralExpression("x");

        var s1 = new VariableExpression.Segment("func", List.of(arg1, arg2));
        var s2 = new VariableExpression.Segment("field");

        var expr = new VariableExpression(List.of(s1, s2));

        assertEquals(".func(10, 'x').field", visitor.visitVariable(expr));
    }

    @Test
    void testFunction_withoutNamespace_noArgs() {
        var expr = new FunctionCallExpression("", "len", List.of());
        assertEquals("len", visitor.visitFunction(expr));
    }

    @Test
    void testFunction_withNamespace_withArgs() {
        var expr = new FunctionCallExpression(
                "math",
                "max",
                List.of(
                        new LiteralExpression(10),
                        new LiteralExpression(5)
                )
        );
        assertEquals("(math:max 10, 5)", visitor.visitFunction(expr));
    }

    @Test
    void testPipe() {
        var expr = new PipeExpression(
                new LiteralExpression(10),
                List.of(
                        new FunctionCallExpression("", "double", List.of()),
                        new FunctionCallExpression("str", "toString", List.of())
                )
        );

        assertEquals("(10 | double | str:toString)", visitor.visitPipe(expr));
    }

    @Test
    void testTernary() {
        var expr = new TernaryExpression(
                new LiteralExpression(true),
                new LiteralExpression("yes"),
                new LiteralExpression("no")
        );

        assertEquals("(true ? 'yes' : 'no')", visitor.visitTernary(expr));
    }
}