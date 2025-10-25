package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.parser.api.Expression;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * AST-like container for compiled template nodes.
 * Used by {@link io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate}.
 *
 * @author sibmaks
 */
final class Nodes {

    /**
     * Represents a spread expression node within an array or object context.
     * <p>
     * Corresponds to {@code {{. expr}}} — inserts or merges evaluated content.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class SpreadNode {
        private final Expression expression;
    }

    /**
     * Represents a conditional insertion node.
     * <p>
     * Corresponds to {@code {{? expr}}} — the expression is evaluated,
     * and the result is inserted only if non-null.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class CondNode {
        private final Expression expression;
    }

    /**
     * Represents a compiled object node that may contain static fields,
     * dynamic keys, and spread expressions.
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class CompiledObject {
        private final List<Entry> entries;

        public interface Entry {
        }

        /**
         * Represents a standard object key-value entry.
         */
        @Getter
        @Builder
        @ToString
        @AllArgsConstructor
        public static final class Field implements Entry {
            private final Object key;
            private final Object value;
        }

        /**
         * Represents a spread entry inside an object.
         * <p>
         * Corresponds to {@code {{. expr}}} — merges map fields from the evaluated expression.
         * </p>
         */
        @Getter
        @Builder
        @ToString
        @AllArgsConstructor
        public static final class Spread implements Entry {
            private final Expression expression;
        }
    }

    /**
     * Represents a static node that holds a literal or constant value.
     */
    @Getter
    @RequiredArgsConstructor
    public static final class StaticNode {
        private final Object value;
    }

    /**
     * Represents a {@code case} construct in a template.
     * <p>
     * Corresponds to syntax like {@code varName case <expr>}, including branches and optional {@code else}.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class CaseDefinition {
        private final Expression switchExpr;
        private final Map<Expression, Object> branches;
        private final Object thenNode;
        private final Object elseNode;
    }

    /**
     * Represents a {@code range} construct in a template.
     * <p>
     * Corresponds to syntax like {@code varName range item,index of <expr>},
     * defining loop variables and the body node to be repeated.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class RangeDefinition {
        private final String item;
        private final String index;
        private final Expression sourceExpr;
        private final Object bodyNode;
    }
}
