package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.TemplateExecutionVisitor;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link CompiledTemplate} that executes a compiled template AST.
 * <p>
 * Evaluates all precompiled definitions and renders the template by traversing
 * the AST using {@link TemplateExecutionVisitor}.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor
final class CompiledTemplateImpl implements CompiledTemplate {
    /**
     * Evaluator used to compute expressions and variable values during rendering.
     */
    private final TemplateEvaluator evaluator;

    /**
     * The list of compiled internal variables.
     */
    private final List<InternalVariable> internalVariables;

    /**
     * The root abstract syntax tree node representing the compiled template.
     */
    private final AstNode compiledTemplate;

    @Override
    public Object render(Map<String, Object> context) {
        var local = new LinkedHashMap<>(context);
        var executor = new TemplateExecutionVisitor(evaluator, local);
        evalDefinitions(executor, local);
        var selectedValue = compiledTemplate.accept(executor);
        return selectedValue.getValue();
    }

    private void evalDefinitions(
            TemplateExecutionVisitor executor,
            Map<String, Object> context
    ) {
        for (var variable : internalVariables) {
            var value = variable.getValue();
            var selectedValue = value.accept(executor);
            if (!selectedValue.isEmpty()) {
                context.put(variable.getName(), selectedValue.getValue());
            }
        }
    }

}