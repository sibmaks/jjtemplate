package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.impl.InternalVariable;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
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
            var nodeName = internalVariable.getName();
            if (!(nodeName instanceof ConstantTemplateExpression)) {
                continue;
            }
            var staticNodeName = (ConstantTemplateExpression) nodeName;
            var value = internalVariable.getValue();
            if (!(value instanceof ConstantTemplateExpression)) {
                continue;
            }
            var staticNodeValue = (ConstantTemplateExpression) value;

            staticVariables.put(String.valueOf(staticNodeName.getValue()), staticNodeValue.getValue());
        }

        if (staticVariables.isEmpty()) {
            return compiledTemplate;
        }

        var expresssionInliner = new TemplateExpressionVariableInliner(staticVariables);

        var anyInlined = false;

        var inlinedVariables = new ArrayList<InternalVariable>(internalVariables.size());
        for (var internalVariable : internalVariables) {
            var name = internalVariable.getName();
            var inlinedName = name.visit(expresssionInliner);
            var wasInlined = name != inlinedName;

            var value = internalVariable.getValue();
            var inlinedValue = value.visit(expresssionInliner);
            wasInlined |= value != inlinedValue;

            if (wasInlined) {
                anyInlined = true;
                var foldedInternalVariable = InternalVariable.builder()
                        .name(inlinedName)
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
