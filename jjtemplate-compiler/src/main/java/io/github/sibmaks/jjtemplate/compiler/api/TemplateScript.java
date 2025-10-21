package io.github.sibmaks.jjtemplate.compiler.api;

import lombok.*;

import java.util.List;

/**
 *
 * @author sibmaks
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TemplateScript {
    private List<Definition> definitions;
    private Object template;
}
