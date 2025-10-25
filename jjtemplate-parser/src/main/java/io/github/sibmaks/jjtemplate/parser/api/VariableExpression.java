package io.github.sibmaks.jjtemplate.parser.api;

import lombok.*;

import java.util.List;

/**
 * Represents a variable expression within a template.
 * <p>
 * A variable expression consists of one or more {@link Segment}s, which can represent
 * nested field accesses or chained method calls.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class VariableExpression implements Expression {
    /**
     * The ordered list of segments that form this variable expression.
     */
    public final List<Segment> segments;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    /**
     * Represents a single segment of a variable expression.
     * <p>
     * A segment can be either a field access (e.g. {@code user.name})
     * or a method call (e.g. {@code user.getName()}).
     * </p>
     */
    @ToString
    @EqualsAndHashCode
    public static final class Segment {
        /**
         * The name of the field or method.
         */
        public final String name;
        /**
         * Indicates whether this segment represents a method call.
         * Return {@code true} if the segment is a method call, {@code false} if it is a field access
         */
        @Getter
        public final boolean method;
        /**
         * The list of argument expressions if this segment is a method call; empty otherwise.
         */
        public final List<Expression> args;

        /**
         * Creates a {@code Segment} representing a method call.
         *
         * @param name the method name
         * @param args the list of argument expressions for the method
         */
        public Segment(String name, List<Expression> args) {
            this.name = name;
            this.method = true;
            this.args = args;
        }

        /**
         * Creates a {@code Segment} representing a field access.
         *
         * @param name the field name
         */
        public Segment(String name) {
            this.name = name;
            this.method = false;
            this.args = List.of();
        }
    }
}

