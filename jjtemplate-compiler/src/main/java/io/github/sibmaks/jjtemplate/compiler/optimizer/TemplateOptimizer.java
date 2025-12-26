package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;

/**
 *
 * Performs optimization passes on the compiled template node and its definitions.
 * <p>
 * The optimizer repeatedly applies constant inlining, dead-definition elimination,
 * and reachability analysis until no further changes occur.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface TemplateOptimizer {

    /**
     * Optimizes a compiled template and its definitions by applying.
     * <p>
     * The optimization process iterates until a stable state is reached (no further changes).
     * </p>
     *
     * @param compiledTemplate compiled template
     * @return a {@link CompiledTemplateImpl} containing optimized definitions and the optimized template
     */
    CompiledTemplateImpl optimize(CompiledTemplateImpl compiledTemplate);

}
