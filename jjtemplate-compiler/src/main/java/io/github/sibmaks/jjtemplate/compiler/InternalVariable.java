package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import lombok.*;

/**
 * @since 0.4.1
 * @author sibmaks
 */
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public final class InternalVariable {
    private final String name;
    @Setter
    private AstNode value;
}
