package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.impl.InternalVariable;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder.TemplateExpressionFolder;

import java.util.ArrayList;

/**
 * {@code CompiledTemplateFolder} performs constant folding over a compiled template.
 * <p>
 * The optimizer traverses both:
 * <ul>
 *   <li>internal variable definitions (names and values)</li>
 *   <li>the compiled template expression AST</li>
 * </ul>
 * and replaces sub-expressions that can be evaluated at compile time
 * with their folded (constant) equivalents.
 * <p>
 * Folding is performed using {@link TemplateExpressionFolder} and is applied
 * uniformly to variable names, variable values, and the template root expression.
 * <p>
 * If no folding occurs, the original {@link CompiledTemplateImpl} instance
 * is returned. Otherwise, a new instance with folded expressions is created.
 * <p>
 * This optimizer is typically executed early in the optimization pipeline
 * to reduce AST complexity before further optimizations such as variable
 * inlining or unused variable elimination.
 *
 * @author sibmaks
 * @see TemplateOptimizer
 * @see TemplateExpressionFolder
 * @see CompiledTemplateImpl
 * @since 0.5.0
 */
public final class CompiledTemplateFolder implements TemplateOptimizer {
    private final TemplateExpressionFolder folder;

    /**
     * Creates a new compiled template folder.
     * <p>
     * Internally initializes a {@link TemplateExpressionFolder} instance
     * that is reused for folding all expressions during optimization.
     * <p>
     * The folder is stateless with respect to a particular template
     * and can be safely used to process multiple compiled templates
     * sequentially.
     */
    public CompiledTemplateFolder() {
        this.folder = new TemplateExpressionFolder();
    }

    @Override
    public CompiledTemplateImpl optimize(CompiledTemplateImpl compiledTemplate) {
        var internalVariables = compiledTemplate.getInternalVariables();

        var anyFolded = false;

        var foldedVariables = new ArrayList<InternalVariable>(internalVariables.size());
        for (var internalVariable : internalVariables) {
            var name = internalVariable.getName();
            var foldedName = name.visit(folder);
            var wasFolded = name != foldedName;

            var value = internalVariable.getValue();
            var foldedValue = value.visit(folder);
            wasFolded |= value != foldedValue;

            if (wasFolded) {
                anyFolded = true;
                var foldedInternalVariable = InternalVariable.builder()
                        .name(foldedName)
                        .value(foldedValue)
                        .build();
                foldedVariables.add(foldedInternalVariable);
            } else {
                foldedVariables.add(internalVariable);
            }
        }

        var astTemplate = compiledTemplate.getCompiledTemplate();
        var folded = astTemplate.visit(folder);
        if (astTemplate != folded) {
            astTemplate = folded;
            anyFolded = true;
        }

        if (anyFolded) {
            return new CompiledTemplateImpl(
                    foldedVariables,
                    astTemplate
            );
        }
        return compiledTemplate;
    }

}
