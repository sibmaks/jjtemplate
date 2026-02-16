package io.github.sibmaks.jjtemplate.parser.parser;

import io.github.sibmaks.jjtemplate.parser.api.Expression;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Lightweight template parse representation used by the frontend parser.
 * <p>
 * This class mirrors the public API surface expected by the compiler without
 * relying on external parser generators.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class JJTemplateParser {
    private JJTemplateParser() {
    }

    /**
     * Supported interpolation kinds.
     */
    public enum InterpolationType {
        EXPRESSION,
        CONDITION,
        SPREAD
    }

    /**
     * Marker interface for template parts.
     */
    public interface TemplatePart {
    }

    /**
     * Root template context consisting of template parts.
     */
    @Getter
    public static final class TemplateContext {
        private final List<TemplatePart> parts;

        /**
         * Creates a template context from parsed parts.
         *
         * @param parts template parts; {@code null} is treated as an empty list
         */
        public TemplateContext(List<TemplatePart> parts) {
            this.parts = parts == null ? List.of() : List.copyOf(parts);
        }

    }

    /**
     * Text template part.
     */
    @Getter
    @AllArgsConstructor
    public static final class TextPart implements TemplatePart {
        private final String text;
    }

    /**
     * Interpolation template part.
     */
    @Getter
    @AllArgsConstructor
    public static final class InterpolationPart implements TemplatePart {
        private final InterpolationType type;
        private final Expression expression;
    }
}
