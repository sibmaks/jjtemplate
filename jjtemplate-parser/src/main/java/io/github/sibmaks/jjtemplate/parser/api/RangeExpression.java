package io.github.sibmaks.jjtemplate.parser.api;

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
public final class RangeExpression implements Expression {
    /**
     * The name or key expression of the range.
     */
    public final Expression name;
    /**
     * First iteration variable: item for collections and arrays, key for maps.
     */
    public final String firstVariableName;
    /**
     * Second iteration variable: index for collections and arrays, value for maps.
     */
    public final String secondVariableName;
    /**
     * The source expression providing the collection, array, or map to iterate.
     */
    public final Expression source;

    /**
     * Creates a range expression.
     *
     * @param name               range result name
     * @param firstVariableName  item variable for collections and arrays, key variable for maps
     * @param secondVariableName index variable for collections and arrays, value variable for maps
     * @param source             range source expression
     */
    public RangeExpression(
            Expression name,
            String firstVariableName,
            String secondVariableName,
            Expression source
    ) {
        this.name = name;
        this.firstVariableName = firstVariableName;
        this.secondVariableName = secondVariableName;
        this.source = source;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitRange(this);
    }
}
