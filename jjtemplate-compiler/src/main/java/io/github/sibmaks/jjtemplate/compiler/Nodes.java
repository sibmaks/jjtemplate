package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstVisitor;
import io.github.sibmaks.jjtemplate.parser.api.Expression;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * AST-like container for compiled template nodes.
 * Used by {@link io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate}.
 *
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Nodes {

    /**
     * Represents an expression node in the AST.
     * <p>
     * Wraps a parsed {@link io.github.sibmaks.jjtemplate.parser.api.Expression}
     * and allows deferred evaluation at render time.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class ExpressionNode implements AstNode {
        private final Expression expression;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitExpression(this);
        }

    }

    /**
     * Represents a sequential list of AST nodes.
     * <p>
     * Used to represent concatenated template fragments or list literals.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class ListNode implements AstNode {
        private final List<AstNode> astNodes;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitList(this);
        }

    }

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
    public static final class SpreadNode implements AstNode {
        private final Expression expression;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitSpread(this);
        }

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
    public static final class CondNode implements AstNode {
        private final Expression expression;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitCond(this);
        }

    }

    /**
     * Represents a compiled object node that may contain static fields,
     * dynamic keys, and spread expressions.
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class CompiledObject implements AstNode {
        private final List<Entry> entries;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitObject(this);
        }

        public interface Entry {
        }

        /**
         * Represents a standard object key-value entry. Key or value must be dynamic.
         */
        @Getter
        @Builder
        @ToString
        @AllArgsConstructor
        public static final class Field implements Entry {
            private final AstNode key;
            private final AstNode value;
        }

        /**
         * Represents a standard object key-value entry with static key and value.
         */
        @Getter
        @Builder
        @ToString
        @AllArgsConstructor
        public static final class StaticField implements Entry {
            private final String key;
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
    @Builder
    @ToString
    @RequiredArgsConstructor
    public static final class StaticNode implements AstNode {
        private static final StaticNode EMPTY_INSTANCE = StaticNode.builder()
                .empty(true)
                .build();
        private static final StaticNode EMPTY_SPREAD_INSTANCE = StaticNode.builder()
                .empty(true)
                .spread(true)
                .build();
        private static final StaticNode EMPTY_COND_INSTANCE = StaticNode.builder()
                .empty(true)
                .cond(true)
                .build();

        private final boolean empty;
        private final boolean spread;
        private final boolean cond;
        private final Object value;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitStatic(this);
        }

        public static StaticNode empty() {
            return EMPTY_INSTANCE;
        }

        public static StaticNode notCondition() {
            return EMPTY_COND_INSTANCE;
        }

        public static StaticNode notSpread() {
            return EMPTY_SPREAD_INSTANCE;
        }

        public static StaticNode of(Object value) {
            return StaticNode.builder()
                    .value(value)
                    .build();
        }

        public static StaticNode ofCondition(Object value) {
            return StaticNode.builder()
                    .value(value)
                    .cond(true)
                    .build();
        }

        public static StaticNode ofSpread(Object value) {
            return StaticNode.builder()
                    .value(value)
                    .spread(true)
                    .build();
        }
    }

    /**
     * Represents a {@code switch} construct in a template.
     * <p>
     * Corresponds to syntax like {@code varName switch <expr>}, including branches and optional {@code else}.
     * </p>
     */
    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static final class SwitchDefinition implements AstNode {
        private final Expression switchExpr;
        private final Map<Expression, AstNode> branches;
        private final AstNode thenNode;
        private final AstNode elseNode;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitSwitch(this);
        }

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
    public static final class RangeDefinition implements AstNode {
        private final String item;
        private final String index;
        private final Expression sourceExpr;
        private final AstNode bodyNode;

        @Override
        public <R> R accept(AstVisitor<R> visitor) {
            return visitor.visitRange(this);
        }

    }
}
