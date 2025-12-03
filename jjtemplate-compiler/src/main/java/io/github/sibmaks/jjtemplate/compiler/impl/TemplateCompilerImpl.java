package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.*;
import io.github.sibmaks.jjtemplate.compiler.optimizer.CompiledTemplateFolder;
import io.github.sibmaks.jjtemplate.compiler.optimizer.TemplateOptimizer;
import io.github.sibmaks.jjtemplate.compiler.optimizer.UnusedVariableNodeEliminator;
import io.github.sibmaks.jjtemplate.compiler.optimizer.VariableNodeInliner;
import io.github.sibmaks.jjtemplate.compiler.runtime.RootTemplateExpressionFactory;
import io.github.sibmaks.jjtemplate.compiler.runtime.TemplateExpressionFactory;
import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateTypeInferenceVisitor;
import io.github.sibmaks.jjtemplate.frontend.ExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of the {@link TemplateCompiler} interface.
 * <p>
 * Responsible for compiling template scripts into executable node structures.
 * It handles parsing of template expressions, variable definitions, ranges,
 * and conditional cases, then applies optimization passes to produce a
 * {@link CompiledTemplate} ready for rendering.
 * </p>
 *
 * <p>Main compilation stages:</p>
 * <ol>
 *   <li>Parsing source definitions and the main template into {@link TemplateExpression}.</li>
 *   <li>Building a {@link CompiledTemplateImpl} for efficient runtime rendering.</li>
 *   <li>Optimizing the node using {@link TemplateOptimizer} (constant folding, dead-code elimination, etc.).</li>
 * </ol>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateCompilerImpl implements TemplateCompiler {

    private final List<TemplateOptimizer> optimizers;
    private final RootTemplateExpressionFactory rootTemplateExpressionFactory;

    /**
     * Creates a template compiler configured with the specified compile options.
     *
     * @param options compilation settings controlling optimization and evaluation behavior
     */
    public TemplateCompilerImpl(TemplateCompileOptions options) {
        var expressionFactory = new TemplateExpressionFactory(options.getEvaluationOptions());
        var expressionParser = new ExpressionParser();
        this.rootTemplateExpressionFactory = new RootTemplateExpressionFactory(
                new TemplateTypeInferenceVisitor(),
                expressionFactory,
                expressionParser
        );
        this.optimizers = new ArrayList<>();
        if (options.isOptimize()) {
            optimizers.add(new CompiledTemplateFolder());
            optimizers.add(new VariableNodeInliner());
            optimizers.add(new UnusedVariableNodeEliminator());
        }
    }

    @Override
    public CompiledTemplate compile(TemplateScript script) {
        var defs = Optional.ofNullable(script.getDefinitions())
                .orElseGet(List::of);
        var template = script.getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("'template' field required");
        }

        var internalVariables = compileInternalVariables(defs);

        var templateNode = rootTemplateExpressionFactory.compile(template);
        var compiledTemplate = new CompiledTemplateImpl(internalVariables, templateNode);
        var repeat = false;
        do {
            repeat = false;
            for (var optimizer : optimizers) {
                var was = compiledTemplate;
                compiledTemplate = optimizer.optimize(compiledTemplate);
                if (was != compiledTemplate) {
                    repeat = true;
                }
            }
        } while (repeat);
        return buildCompiledTemplate(compiledTemplate.getCompiledTemplate(), compiledTemplate.getInternalVariables());
    }

    private List<InternalVariable> compileInternalVariables(List<Definition> defs) {
        var internalVariables = new ArrayList<InternalVariable>();
        for (var def : defs) {
            var compiled = rootTemplateExpressionFactory.compile(def);
            var objectVariables = (ObjectTemplateExpression) compiled;
            for (var element : objectVariables.getElements()) {
                if (element instanceof ObjectFieldElement) {
                    var objectFieldElement = (ObjectFieldElement) element;
                    var internalVariable = InternalVariable.builder()
                            .name(objectFieldElement.getKey())
                            .value(objectFieldElement.getValue())
                            .build();
                    internalVariables.add(internalVariable);
                } else {
                    throw new TemplateEvalException(String.format("Unknown object field element type: %s", element.getClass()));
                }
            }
        }
        return internalVariables;
    }

    private CompiledTemplate buildCompiledTemplate(
            TemplateExpression compiledTemplate,
            List<InternalVariable> internalVariables
    ) {
        if (compiledTemplate instanceof ConstantTemplateExpression) {
            var constantExpression = (ConstantTemplateExpression) compiledTemplate;
            var value = constantExpression.getValue();
            return new StaticCompiledTemplateImpl(value);
        }
        return new CompiledTemplateImpl(internalVariables, compiledTemplate);
    }
}
