package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import lombok.*;

/**
 * Represents an internal variable used in a compiled template.
 * Stores the variable name and its associated node value,
 * which may be transformed by optimizer passes.
 *
 * @author sibmaks
 * @since 0.4.1
 */
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public final class InternalVariable {
    /**
     * The name of the internal template variable.
     */
    private final TemplateExpression name;
    /**
     * The compiled node associated with the variable.
     */
    private final TemplateExpression value;
}
