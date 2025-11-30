package io.github.sibmaks.jjtemplate.compiler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;

import java.io.IOException;

/**
 * Benchmark template script provider
 *
 * @author sibmaks
 * @since 0.4.1
 */
public final class Templates {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    /**
     * Get template script by scenario
     *
     * @param scenario scenario
     * @return template script
     */
    public static TemplateScript byName(Scenario scenario) {
        if (scenario.inline) {
            return load(String.format("templates/inline/%s.json", scenario.template.name().toLowerCase()));
        }
        return load(String.format("templates/%s.json", scenario.template.name().toLowerCase()));
    }

    private static TemplateScript load(String path) {
        try (var is = Templates.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return OBJECT_MAPPER.readValue(is, TemplateScript.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
