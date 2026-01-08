package io.github.sibmaks.jjtemplate.compiler.runtime.expression;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author sibmaks
 */
class ConstantTemplateExpressionTest {

    @Test
    void apply() {
        var value = UUID.randomUUID().toString();
        var expression = new ConstantTemplateExpression(value);
        var actual = expression.apply(null);
        assertEquals(value, actual);
    }

    @Test
    void visit() {
        var value = UUID.randomUUID().toString();
        var expression = new ConstantTemplateExpression(value);
        TemplateExpressionVisitor<Void> visitor = mock();
        expression.visit(visitor);
        verify(visitor)
                .visit(expression);
    }

}