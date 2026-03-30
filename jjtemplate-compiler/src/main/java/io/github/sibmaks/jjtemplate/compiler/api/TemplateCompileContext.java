package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.List;
import java.util.Optional;

/**
 * Provides compile-time type information for template variables.
 *
 * @author sibmaks
 * @since 0.6.0
 */
public interface TemplateCompileContext {

    /**
     * Returns known possible runtime types for the specified variable name.
     *
     * @param variableName variable name to resolve
     * @return known possible types, or an empty result if the type is unknown
     */
    Optional<List<Class<?>>> lookupTypes(String variableName);

    /**
     * Returns the validation mode used for compile-time type checks.
     *
     * @return validation mode
     */
    default TemplateTypeValidationMode validationMode() {
        return TemplateTypeValidationMode.SOFT;
    }
}
