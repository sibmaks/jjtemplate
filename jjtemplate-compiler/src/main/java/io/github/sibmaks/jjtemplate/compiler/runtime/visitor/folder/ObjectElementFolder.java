package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sibmaks
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class ObjectElementFolder implements ObjectElementVisitor<List<ObjectElement>> {
    private final TemplateExpressionFolder folder;

    @Override
    public List<ObjectElement> visit(ObjectFieldElement element) {
        var key = element.getKey();
        var foldedKey = key.visit(folder);
        var anyFolded = foldedKey != key;

        var value = element.getValue();
        var foldedValue = value.visit(folder);
        anyFolded |= foldedValue != value;

        if (foldedKey instanceof ConstantTemplateExpression &&
                foldedValue instanceof ConstantTemplateExpression) {
            var constantKeyExpression = (ConstantTemplateExpression) foldedKey;
            var constantValueExpression = (ConstantTemplateExpression) foldedValue;
            return List.of(new ObjectStaticFieldElement(
                    String.valueOf(constantKeyExpression.getValue()),
                    constantValueExpression.getValue()
            ));
        }

        if (anyFolded) {
            return List.of(new ObjectFieldElement(foldedKey, foldedValue));
        }

        return List.of(element);
    }

    @Override
    public List<ObjectElement> visit(SpreadObjectElement element) {
        var source = element.getSource();
        var foldedSource = source.visit(folder);
        var anyFolded = foldedSource != source;

        if (foldedSource instanceof ConstantTemplateExpression) {
            var constantExpression = (ConstantTemplateExpression) foldedSource;
            var expressionValue = (Map<String, Object>) constantExpression.getValue();
            var elements = new ArrayList<ObjectElement>(expressionValue.size());
            for (var entry : expressionValue.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                var itemElement = new ObjectStaticFieldElement(key, value);
                elements.add(itemElement);
            }
            return elements;
        }

        if (anyFolded) {
            return List.of(new SpreadObjectElement(foldedSource));
        }

        return List.of(element);
    }

    @Override
    public List<ObjectElement> visit(ObjectStaticFieldElement element) {
        return List.of(element);
    }
}
