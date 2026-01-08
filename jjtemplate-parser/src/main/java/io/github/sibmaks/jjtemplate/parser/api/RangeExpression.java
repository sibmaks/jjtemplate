package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a range expression within a template.
 * <p>
 * Range expressions define an iteration source and variable names
 * used when iterating.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class RangeExpression implements Expression {
    /**
     * The name or key expression of the range.
     */
    public final Expression name;
    /**
     * Variable name for the current item.
     */
    public final String itemVariableName;
    /**
     * Variable name for the current index.
     */
    public final String indexVariableName;
    /**
     * The source expression providing the collection to iterate.
     */
    public final Expression source;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitRange(this);
    }
}
