package io.github.sibmaks.jjtemplate.compiler.api;

import io.github.sibmaks.jjtemplate.compiler.runtime.TemplateEvaluationOptions;
import lombok.*;

import java.util.Locale;

/**
 * Defines configuration options used during template compilation.
 * <p>
 * These options control both the compilation phase (e.g. optimization)
 * and the runtime evaluation settings through {@link TemplateEvaluationOptions}.
 * <p>
 * This class is immutable and can be created using the {@link Builder} pattern.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * var options = TemplateCompileOptions.builder()
 *     .optimize(true)
 *     .evaluationOptions(
 *         TemplateEvaluationOptions.builder()
 *             .locale(Locale.ENGLISH)
 *             .build()
 *     )
 *     .build();
 * }</pre>
 *
 * @author sibmaks
 * @since 0.1.2
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemplateCompileOptions {
    /**
     * Whether the compiler should perform static optimization passes.
     * <p>
     * Optimization may include constant folding, unused definition elimination,
     * and expression simplification. Enabled by default.
     */
    @Builder.Default
    private final boolean optimize = true;
    /**
     * When enabled, definition keys are parsed as expressions. If parsing fails,
     * the compiler logs a warning and falls back to using the raw key string as
     * a constant.
     */
    @Builder.Default
    private final boolean definitionKeyExpressionFallback = false;
    /**
     * Evaluation-specific configuration options,
     * such as locale and custom function behavior.
     * <p>
     * Defaults to {@link Locale#ROOT}.
     */
    @NonNull
    @Builder.Default
    private final TemplateEvaluationOptions evaluationOptions = TemplateEvaluationOptions.getDefault();
}
