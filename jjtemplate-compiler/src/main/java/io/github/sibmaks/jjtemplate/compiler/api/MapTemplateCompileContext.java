package io.github.sibmaks.jjtemplate.compiler.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Map-backed implementation of {@link TemplateCompileContext}.
 *
 * @author sibmaks
 * @since 0.6.0
 */
@ToString
@EqualsAndHashCode
public final class MapTemplateCompileContext implements TemplateCompileContext {
    private final Map<String, List<Class<?>>> types;
    private final TemplateTypeValidationMode validationMode;

    /**
     * Creates a soft-validation compile context backed by the provided map.
     *
     * @param types variable type mapping
     */
    public MapTemplateCompileContext(Map<String, List<Class<?>>> types) {
        this(types, TemplateTypeValidationMode.SOFT);
    }

    /**
     * Creates a compile context backed by the provided map and validation mode.
     *
     * @param types variable type mapping
     * @param validationMode validation mode to use
     */
    public MapTemplateCompileContext(
            Map<String, List<Class<?>>> types,
            TemplateTypeValidationMode validationMode
    ) {
        this.types = copy(types);
        this.validationMode = validationMode;
    }

    @Override
    public Optional<List<Class<?>>> lookupTypes(String variableName) {
        return Optional.ofNullable(types.get(variableName));
    }

    @Override
    public TemplateTypeValidationMode validationMode() {
        return validationMode;
    }

    private static Map<String, List<Class<?>>> copy(Map<String, List<Class<?>>> types) {
        var copy = new LinkedHashMap<String, List<Class<?>>>(types.size());
        for (var entry : types.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }
}
