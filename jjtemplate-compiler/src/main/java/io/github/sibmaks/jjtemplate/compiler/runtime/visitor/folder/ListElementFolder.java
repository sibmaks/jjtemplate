package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.*;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class ListElementFolder implements ListElementVisitor<List<ListElement>> {
    private final TemplateExpressionFolder folder;

    @Override
    public List<ListElement> visit(ConditionListElement element) {
        var source = element.getSource();
        var foldedSource = source.visit(folder);
        var anyFolded = foldedSource != source;

        if (foldedSource instanceof ConstantTemplateExpression) {
            var constantExpression = (ConstantTemplateExpression) foldedSource;
            var expressionValue = constantExpression.getValue();
            if (expressionValue == null) {
                return List.of();
            }
            return List.of(new ListStaticItemElement(expressionValue));
        }

        if (anyFolded) {
            return List.of(new ConditionListElement(foldedSource));
        }

        return List.of(element);
    }

    @Override
    public List<ListElement> visit(DynamicListElement element) {
        var value = element.getValue();
        var foldedValue = value.visit(folder);
        var anyFolded = foldedValue != value;

        if (foldedValue instanceof ConstantTemplateExpression) {
            var constantExpression = (ConstantTemplateExpression) foldedValue;
            return List.of(new ListStaticItemElement(constantExpression.getValue()));
        }

        if (anyFolded) {
            return List.of(new DynamicListElement(foldedValue));
        }

        return List.of(element);
    }

    @Override
    public List<ListElement> visit(SpreadListElement element) {
        var source = element.getSource();
        var foldedSource = source.visit(folder);
        var anyFolded = foldedSource != source;

        if (foldedSource instanceof ConstantTemplateExpression) {
            var constantExpression = (ConstantTemplateExpression) foldedSource;
            var expressionValue = constantExpression.getValue();
            if (expressionValue == null) {
                return List.of();
            }
            if (expressionValue instanceof Collection) {
                var collection = (Collection<?>) expressionValue;
                var elements = new ArrayList<ListElement>(collection.size());
                for (var item : collection) {
                    elements.add(new ListStaticItemElement(item));
                }
                return elements;
            }
            if (expressionValue.getClass().isArray()) {
                var length = Array.getLength(expressionValue);
                var elements = new ArrayList<ListElement>(length);
                for (int i = 0; i < length; i++) {
                    var item = Array.get(expressionValue, i);
                    elements.add(new ListStaticItemElement(item));
                }
                return elements;
            }
            return List.of(new ListStaticItemElement(expressionValue));
        }

        if (anyFolded) {
            return List.of(new SpreadListElement(foldedSource));
        }

        return List.of(element);
    }

    @Override
    public List<ListElement> visit(ListStaticItemElement element) {
        return List.of(element);
    }
}
