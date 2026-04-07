package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.DynamicListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListStaticItemElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.SpreadListElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sibmaks
 */
class ListElementFolderTest {

    @Test
    void spreadListElementShouldExpandConstantArray() {
        var folder = new TemplateExpressionFolder();
        var element = new SpreadListElement(new ConstantTemplateExpression(new String[]{"a", "b"}));
        var listElementFolder = new ListElementFolder(folder);

        var folded = listElementFolder.visit(element);

        assertEquals(2, folded.size());
        assertEquals("a", assertInstanceOf(ListStaticItemElement.class, folded.get(0)).getValue());
        assertEquals("b", assertInstanceOf(ListStaticItemElement.class, folded.get(1)).getValue());
    }

    @Test
    void spreadListElementShouldReturnUpdatedSpreadWhenSourceChanges() {
        var folder = mock(TemplateExpressionFolder.class);
        TemplateExpression source = mock();
        TemplateExpression foldedSource = mock();
        when(source.visit(folder)).thenReturn(foldedSource);

        var listElementFolder = new ListElementFolder(folder);

        var folded = listElementFolder.visit(new SpreadListElement(source));

        assertEquals(1, folded.size());
        var spread = assertInstanceOf(SpreadListElement.class, folded.get(0));
        assertSame(foldedSource, spread.getSource());
    }

    @Test
    void dynamicListElementShouldReturnSameWhenValueUnchanged() {
        var folder = new TemplateExpressionFolder();
        var element = new DynamicListElement(new ConstantTemplateExpression("value"));
        var listElementFolder = new ListElementFolder(folder);

        var folded = listElementFolder.visit(assertInstanceOf(DynamicListElement.class, element));

        assertEquals(1, folded.size());
        assertInstanceOf(ListStaticItemElement.class, folded.get(0));
    }
}
