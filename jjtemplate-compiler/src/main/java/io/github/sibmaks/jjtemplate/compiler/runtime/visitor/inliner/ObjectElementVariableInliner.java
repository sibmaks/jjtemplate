package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.*;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public class ObjectElementVariableInliner implements ObjectElementVisitor<ObjectElement> {
    private final TemplateExpressionVariableInliner inliner;

    @Override
    public ObjectElement visit(ObjectFieldElement element) {
        var key = element.getKey();
        var inlinedKey = key.visit(inliner);
        var anyInlined = inlinedKey != key;

        var value = element.getValue();
        var inlinedValue = value.visit(inliner);
        anyInlined |= inlinedValue != value;

        if (anyInlined) {
            return new ObjectFieldElement(inlinedKey, inlinedValue);
        }

        return element;
    }

    @Override
    public ObjectElement visit(SpreadObjectElement element) {
        var source = element.getSource();
        var inlinedSource = source.visit(inliner);
        var anyInlined = inlinedSource != source;

        if (anyInlined) {
            return new SpreadObjectElement(inlinedSource);
        }

        return element;
    }

    @Override
    public ObjectElement visit(ObjectStaticFieldElement element) {
        return element;
    }
}
