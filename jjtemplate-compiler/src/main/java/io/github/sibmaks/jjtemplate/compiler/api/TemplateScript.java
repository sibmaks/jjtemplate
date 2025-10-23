package io.github.sibmaks.jjtemplate.compiler.api;

import lombok.*;

import java.util.List;

/**
 * Represents a template script containing both the template content
 * and its associated definitions.
 * <p>
 * A {@code TemplateScript} serves as the input for {@link TemplateCompiler},
 * which compiles it into an executable {@link CompiledTemplate}.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TemplateScript {
    /**
     * The list of variable and function definitions available in the template.
     */
    private List<Definition> definitions;

    /**
     * The parsed or raw representation of the template content.
     */
    private Object template;

}
