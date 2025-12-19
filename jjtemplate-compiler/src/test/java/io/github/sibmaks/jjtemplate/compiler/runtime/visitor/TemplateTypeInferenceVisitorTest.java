package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class TemplateTypeInferenceVisitorTest {
    @InjectMocks
    private TemplateTypeInferenceVisitor visitor;

    @Test
    void visitTemplate_singleExpression_returnsSameType() {
        JJTemplateParser.TemplateContext context = mock();

        when(context.getChildCount())
                .thenReturn(1);

        JJTemplateParser.TemplateContext child = mock("child");
        when(context.getChild(0))
                .thenReturn(child);

        var childType = mock(TemplateType.class);
        when(child.accept(visitor))
                .thenReturn(childType);

        var result = visitor.visitTemplate(context);

        assertEquals(childType, result);
    }

    @Test
    void visitTemplate_multipleChildren_returnsExpression() {
        JJTemplateParser.TemplateContext context = mock();

        when(context.getChildCount())
                .thenReturn(2);

        JJTemplateParser.TemplateContext child = mock("child");
        when(context.getChild(0))
                .thenReturn(child);

        when(context.getChild(1))
                .thenReturn(child);

        var childType = mock(TemplateType.class);
        when(child.accept(visitor))
                .thenReturn(childType);

        var result = visitor.visitTemplate(context);

        assertEquals(TemplateType.EXPRESSION, result);
    }

    @Test
    void visitTemplate_multipleChildren_oneNull() {
        JJTemplateParser.TemplateContext context = mock();

        when(context.getChildCount())
                .thenReturn(2);

        JJTemplateParser.TemplateContext child = mock("child");
        when(context.getChild(0))
                .thenReturn(child);

        JJTemplateParser.TemplateContext child2 = mock("child2");
        when(context.getChild(1))
                .thenReturn(child2);

        var childType = mock(TemplateType.class);
        when(child.accept(visitor))
                .thenReturn(childType);
        when(child2.accept(visitor))
                .thenReturn(null);

        var result = visitor.visitTemplate(context);

        assertEquals(childType, result);
    }

    @Test
    void visitText() {
        assertEquals(TemplateType.CONSTANT, visitor.visitText(null));
    }

    @Test
    void visitExprInterpolation() {
        JJTemplateParser.ExprInterpolationContext context = mock();
        ParseTree child = mock();
        when(context.getChild(1))
                .thenReturn(child);

        TemplateType templateType = mock();
        when(child.accept(visitor))
                .thenReturn(templateType);

        var actual = visitor.visitExprInterpolation(context);
        assertEquals(templateType, actual);
    }

    @Test
    void visitCondInterpolation() {
        assertEquals(TemplateType.CONDITION, visitor.visitCondInterpolation(null));
    }

    @Test
    void visitSpreadInterpolation() {
        assertEquals(TemplateType.SPREAD, visitor.visitSpreadInterpolation(null));
    }
}