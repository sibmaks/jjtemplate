package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.*;

import java.util.List;
import java.util.Locale;

/**
 * Configuration options controlling how template expressions are evaluated.
 * <p>
 * This class defines runtime evaluation behavior such as locale-dependent string
 * transformations and additional user-defined functions.
 * It is passed into {@link io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator}
 * and propagated through all template evaluations.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Defines the {@link Locale} used by locale-sensitive functions
 *       (e.g., {@code upper}, {@code lower}, {@code formatDate}).</li>
 *   <li>Allows registration of additional custom {@link TemplateFunction}s
 *       beyond the built-in ones.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * var options = TemplateEvaluationOptions.builder()
 *     .locale(Locale.US)
 *     .functions(List.of(new CustomToSnakeCaseFunction()))
 *     .build();
 * }</pre>
 *
 * <p>All instances are immutable and thread-safe.</p>
 *
 * @author sibmaks
 * @see io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator
 * @see io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction
 * @since 0.1.2
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemplateEvaluationOptions {
    /**
     * The locale used for all locale-dependent operations,
     * such as string case transformations and date formatting.
     */
    @NonNull
    private final Locale locale;

    /**
     * A list of user-defined template functions to register in addition to
     * the built-in JJTemplate functions.
     * <p>
     * Each function must have a unique name. Duplicates will cause an
     * {@link IllegalArgumentException} during evaluator initialization.
     * </p>
     */
    @NonNull
    @Builder.Default
    private final List<TemplateFunction<?>> functions = List.of();
}
