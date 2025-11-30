package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @since 0.4.1
 * @author sibmaks
 */
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class InternalVariable {
    private final String name;
    private final AstNode value;
}
