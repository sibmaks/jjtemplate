package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.varusage.VariableUsageCollector;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Removes internal template variables that are never referenced in either
 * the compiled template body or in other variable definitions.
 * <p>
 * This optimization step scans all AST nodes, collects the set of actually
 * referenced variable names, and discards unused definitions. It does not
 * modify the template itself — only the list of internal variables.
 * <p>
 * The pass is idempotent and may be executed repeatedly by the optimizer loop.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class UnusedVariableNodeEliminator implements TemplateOptimizer {

    @Override
    public CompiledTemplateImpl optimize(CompiledTemplateImpl compiledTemplate) {
        var internalVariables = compiledTemplate.getInternalVariables();
        if (internalVariables.isEmpty()) {
            return compiledTemplate;
        }
        var collector = new VariableUsageCollector();

        for (var internalVariable : internalVariables) {
            var key = internalVariable.getKey();
            key.visit(collector);
            var value = internalVariable.getValue();
            value.visit(collector);
        }

        var astNode = compiledTemplate.getCompiledTemplate();
        astNode.visit(collector);

        var usedVariables = collector.getVariables();

        var cleaned = internalVariables.stream()
                .filter(internalVariable -> isUsed(internalVariable, usedVariables))
                .collect(Collectors.toList());

        if (cleaned.size() == internalVariables.size()) {
            return compiledTemplate;
        }

        return new CompiledTemplateImpl(cleaned, astNode);
    }

    private boolean isUsed(ObjectFieldElement internalVariable, Set<String> usedVariables) {
        var nodeKey = internalVariable.getKey();
        if (nodeKey instanceof ConstantTemplateExpression) {
            var constantTemplateExpression = (ConstantTemplateExpression) nodeKey;
            var value = constantTemplateExpression.getValue();
            return usedVariables.contains(String.valueOf(value));
        }
        return true;
    }
}
