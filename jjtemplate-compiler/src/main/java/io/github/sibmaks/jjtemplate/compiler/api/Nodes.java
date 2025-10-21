package io.github.sibmaks.jjtemplate.compiler.api;

import io.github.sibmaks.jjtemplate.parser.api.Expression;
import java.util.*;

/**
 * AST-like container for compiled template nodes.
 * Used by {@link io.github.sibmaks.jjtemplate.compiler.CompiledTemplate}.
 *
 * @author sibmaks
 */
public final class Nodes {

    /** {{. expr}} inside array or object — spread node */
    public static final class SpreadNode {
        private final Expression expression;

        public SpreadNode(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return "SpreadNode{" + expression + '}';
        }
    }

    /** {{? expr}} — conditional insertion node (skip if null) */
    public static final class CondNode {
        private final Expression expression;

        public CondNode(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return "CondNode{" + expression + '}';
        }
    }

    /** Object with spread or dynamic keys */
    public static final class CompiledObject {
        private final List<Entry> entries;

        public CompiledObject(List<Entry> entries) {
            this.entries = entries;
        }

        public List<Entry> getEntries() {
            return entries;
        }

        public interface Entry {}

        /** Normal key-value pair */
        public static final class Field implements Entry {
            private final Object key;
            private final Object value;

            public Field(Object key, Object value) {
                this.key = key;
                this.value = value;
            }

            public Object getKey() {
                return key;
            }

            public Object getValue() {
                return value;
            }

            @Override
            public String toString() {
                return "Field{" + key + "=" + value + '}';
            }
        }

        /** {{. expr}} inside object — merges map fields */
        public static final class Spread implements Entry {
            private final Expression expression;

            public Spread(Expression expression) {
                this.expression = expression;
            }

            public Expression getExpression() {
                return expression;
            }

            @Override
            public String toString() {
                return "Spread{" + expression + '}';
            }
        }
    }

    /** varName case <expr> */
    public static final class CaseDefinition {
        private final Expression switchExpr;
        private final Map<Expression, Object> branches;
        private final Object thenNode;
        private final Object elseNode;

        public CaseDefinition(Expression switchExpr,
                              Map<Expression, Object> branches,
                              Object thenNode,
                              Object elseNode) {
            this.switchExpr = switchExpr;
            this.branches = branches;
            this.thenNode = thenNode;
            this.elseNode = elseNode;
        }

        public Expression getSwitchExpr() {
            return switchExpr;
        }

        public Map<Expression, Object> getBranches() {
            return branches;
        }

        public Object getThenNode() {
            return thenNode;
        }

        public Object getElseNode() {
            return elseNode;
        }
    }

    /** varName range item,index of <expr> */
    public static final class RangeDefinition {
        private final String item;
        private final String index;
        private final Expression sourceExpr;
        private final Object bodyNode;

        public RangeDefinition(String item, String index, Expression sourceExpr, Object bodyNode) {
            this.item = item;
            this.index = index;
            this.sourceExpr = sourceExpr;
            this.bodyNode = bodyNode;
        }

        public String getItem() {
            return item;
        }

        public String getIndex() {
            return index;
        }

        public Expression getSourceExpr() {
            return sourceExpr;
        }

        public Object getBodyNode() {
            return bodyNode;
        }
    }
}
