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

    /**
     * Creates an instance.
     */
    public VariableNodeInliner() {
        // No initialization is required because this implementation is stateless.
    }

    @Override
    public CompiledTemplateImpl optimize(CompiledTemplateImpl compiledTemplate) {
        var internalVariables = compiledTemplate.getInternalVariables();
        if (internalVariables.isEmpty()) {
            return compiledTemplate;
        }

        var anyInlined = false;
        var staticVariables = new HashMap<String, Object>();
        var inlinedVariables = new ArrayList<ObjectFieldElement>(internalVariables.size());
        for (var internalVariable : internalVariables) {
            var expressionInliner = new TemplateExpressionVariableInliner(staticVariables);
            var key = internalVariable.getKey();
            var inlinedKey = key.visit(expressionInliner);
            var wasInlined = key != inlinedKey;

            var value = internalVariable.getValue();
            var inlinedValue = value.visit(expressionInliner);
            wasInlined |= value != inlinedValue;

            ObjectFieldElement inlinedVariable;
            if (wasInlined) {
                anyInlined = true;
                inlinedVariable = ObjectFieldElement.builder()
                        .key(inlinedKey)
                        .value(inlinedValue)
                        .build();
            } else {
                inlinedVariable = internalVariable;
            }
            inlinedVariables.add(inlinedVariable);

            if (inlinedKey instanceof ConstantTemplateExpression) {
                var staticKey = (ConstantTemplateExpression) inlinedKey;
                var variableName = String.valueOf(staticKey.getValue());
                if (inlinedValue instanceof ConstantTemplateExpression) {
                    var staticValue = (ConstantTemplateExpression) inlinedValue;
                    staticVariables.put(variableName, staticValue.getValue());
                } else {
                    staticVariables.remove(variableName);
                }
            }
        }

        var astNode = compiledTemplate.getCompiledTemplate();
        var expressionInliner = new TemplateExpressionVariableInliner(staticVariables);
        var inlined = astNode.visit(expressionInliner);
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
