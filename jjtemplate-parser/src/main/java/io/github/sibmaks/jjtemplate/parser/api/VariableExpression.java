package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author sibmaks
 */
@EqualsAndHashCode
@ToString
public final class VariableExpression implements Expression {
    public final List<Segment> segments;

    public VariableExpression(List<Segment> segments) {
        this.segments = segments;
    }

    @EqualsAndHashCode
    @ToString
    public static final class Segment {
        public final String name;
        @Getter
        public final boolean method;
        public final List<Expression> args;

        public Segment(String name, List<Expression> args) {
            this.name = name;
            this.method = true;
            this.args = args;
        }

        public Segment(String name) {
            this.name = name;
            this.method = false;
            this.args = List.of();
        }
    }
}

