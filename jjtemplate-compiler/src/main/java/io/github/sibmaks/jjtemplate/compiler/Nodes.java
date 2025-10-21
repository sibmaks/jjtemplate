package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.parser.api.Expression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

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
     * {{. expr}} inside array or object — spread node
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class SpreadNode {
        private final Expression expression;
    }

    /**
     * {{? expr}} — conditional insertion node (skip if null)
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class CondNode {
        private final Expression expression;
    }

    /**
     * Object with spread or dynamic keys
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
         * Normal key-value pair
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
         * {{. expr}} inside object — merges map fields
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
     * {@code varName case <expr>}
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
     * {@code varName range item,index of <expr>}
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
