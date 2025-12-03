package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.*;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public class ListElementVariableInliner implements ListElementVisitor<ListElement> {
    private final TemplateExpressionVariableInliner inliner;

    @Override
    public ListElement visit(ConditionListElement element) {
        var source = element.getSource();
        var inlinedSource = source.visit(inliner);
        var anyInlined = inlinedSource != source;

        if (anyInlined) {
            return new ConditionListElement(inlinedSource);
        }

        return element;
    }

    @Override
    public ListElement visit(DynamicListElement element) {
        var value = element.getValue();
        var inlinedValue = value.visit(inliner);
        var anyInlined = inlinedValue != value;

        if (anyInlined) {
            return new DynamicListElement(inlinedValue);
        }

        return element;
    }

    @Override
    public ListElement visit(SpreadListElement element) {
        var source = element.getSource();
        var inlinedSource = source.visit(inliner);
        var anyInlined = inlinedSource != source;

        if (anyInlined) {
            return new SpreadListElement(inlinedSource);
        }

        return element;
    }

    @Override
    public ListElement visit(ListStaticItemElement element) {
        return element;
    }
}
