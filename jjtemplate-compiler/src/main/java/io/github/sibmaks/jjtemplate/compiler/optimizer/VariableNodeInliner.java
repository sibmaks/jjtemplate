package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.inliner.TemplateExpressionVariableInliner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Inlines internal variables whose values are fully static.
 * <p>
 * During optimization, all definitions whose value is a {@link ConstantTemplateExpression}
 * are substituted directly into the node. This eliminates indirection and
 * allows subsequent optimizers to fold expressions further.
 * </p>
 *
 * <p>Only variables proven to be compile-time constants are inlined.</p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class VariableNodeInliner implements TemplateOptimizer {

    @Override
    public CompiledTemplateImpl optimize(CompiledTemplateImpl compiledTemplate) {
        var internalVariables = compiledTemplate.getInternalVariables();
        if (internalVariables.isEmpty()) {
            return compiledTemplate;
        }

        var staticVariables = new HashMap<String, Object>();

        for (var internalVariable : internalVariables) {
            var nodeKey = internalVariable.getKey();
            if (!(nodeKey instanceof ConstantTemplateExpression)) {
                continue;
            }
            var staticNodeKey = (ConstantTemplateExpression) nodeKey;
            var value = internalVariable.getValue();
            if (!(value instanceof ConstantTemplateExpression)) {
                continue;
            }
            var staticNodeValue = (ConstantTemplateExpression) value;

            staticVariables.put(String.valueOf(staticNodeKey.getValue()), staticNodeValue.getValue());
        }

        if (staticVariables.isEmpty()) {
            return compiledTemplate;
        }

        var expresssionInliner = new TemplateExpressionVariableInliner(staticVariables);

        var anyInlined = false;

        var inlinedVariables = new ArrayList<ObjectFieldElement>(internalVariables.size());
        for (var internalVariable : internalVariables) {
            var key = internalVariable.getKey();
            var inlinedKey = key.visit(expresssionInliner);
            var wasInlined = key != inlinedKey;

            var value = internalVariable.getValue();
            var inlinedValue = value.visit(expresssionInliner);
            wasInlined |= value != inlinedValue;

            if (wasInlined) {
                anyInlined = true;
                var foldedInternalVariable = ObjectFieldElement.builder()
                        .key(inlinedKey)
                        .value(inlinedValue)
                        .build();
                inlinedVariables.add(foldedInternalVariable);
            } else {
                inlinedVariables.add(internalVariable);
            }
        }

        var astNode = compiledTemplate.getCompiledTemplate();
        var inlined = astNode.visit(expresssionInliner);
        if (astNode != inlined) {
            anyInlined = true;
            astNode = inlined;
        }

        if (anyInlined) {
            return new CompiledTemplateImpl(inlinedVariables, astNode);
        }
        return compiledTemplate;
    }

}
