package io.github.sibmaks.jjtemplate.compiler.visitor.ast;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Executes the compiled template AST by evaluating expressions and constructing static nodes.
 * <p>
 * This visitor walks through all AST nodes, evaluates expressions using the provided
 * {@link TemplateEvaluator}, and produces corresponding {@link Nodes.StaticNode} results.
 * It supports conditionals, ranges, objects, spreads, and general expressions.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateExecutionVisitor implements AstVisitor<Nodes.StaticNode> {
    /**
     * Evaluator used to compute the values of expressions within the template.
     */
    private final TemplateEvaluator evaluator;

    /**
     * The current variable context used during template execution.
     */
    private final Context context;

    public TemplateExecutionVisitor(
            TemplateEvaluator evaluator,
            Map<String, Object> context
    ) {
        this.evaluator = evaluator;
        this.context = new Context(context);
    }

    @Override
    public Nodes.StaticNode visitStatic(Nodes.StaticNode node) {
        return node;
    }

    @Override
    public Nodes.StaticNode visitSwitch(Nodes.SwitchDefinition node) {
        var switchVal = evaluator.evaluate(node.getSwitchExpr(), context);
        AstNode selected = null;
        var matched = false;
        var branches = node.getBranches();
        for (var branch : branches.entrySet()) {
            var keyVal = evaluator.evaluate(branch.getKey(), context);
            if (Objects.equals(switchVal, keyVal)) {
                selected = branch.getValue();
                matched = true;
                break;
            }
        }
        if (!matched && Boolean.TRUE.equals(switchVal) && node.getThenNode() != null) {
            selected = node.getThenNode();
            matched = true;
        }
        if (!matched) {
            selected = node.getElseNode();
        }
        if (selected != null) {
            return selected.accept(this);
        }
        return Nodes.StaticNode.empty();
    }

    @Override
    public Nodes.StaticNode visitRange(Nodes.RangeDefinition node) {
        var source = evaluator.evaluate(node.getSourceExpr(), context);
        if (source == null) {
            return Nodes.StaticNode.empty();
        }
        if (source instanceof Iterable<?>) {
            var iterable = (Iterable<?>) source;
            return visitRangeIterable(node, iterable);
        } else if (source.getClass().isArray()) {
            return visitRangeArray(node, source);
        }
        throw new IllegalArgumentException("range: expression must be iterable or array");
    }

    private Nodes.StaticNode visitRangeIterable(Nodes.RangeDefinition node, Iterable<?> iterable) {
        var out = new ArrayList<>();
        var i = 0;
        var child = new HashMap<String, Object>(2);
        var itemName = node.getItem();
        var indexName = node.getIndex();
        var bodyNode = node.getBodyNode();
        for (var item : iterable) {
            child.put(itemName, item);
            child.put(indexName, i);
            collectRangeItem(bodyNode, child, out);
            i++;
        }
        return Nodes.StaticNode.of(out);
    }

    private Nodes.StaticNode visitRangeArray(Nodes.RangeDefinition node, Object source) {
        var out = new ArrayList<>();
        var len = Array.getLength(source);
        var child = new HashMap<String, Object>(2);
        var itemName = node.getItem();
        var indexName = node.getIndex();
        var bodyNode = node.getBodyNode();
        for (int i = 0; i < len; i++) {
            var item = Array.get(source, i);
            child.put(itemName, item);
            child.put(indexName, i);
            collectRangeItem(bodyNode, child, out);
        }
        return Nodes.StaticNode.of(out);
    }

    private void collectRangeItem(AstNode bodyNode, Map<String, Object> child, List<Object> out) {
        try {
            context.in(child);
            var selectedValue = bodyNode.accept(this);
            if (!selectedValue.isEmpty()) {
                out.add(selectedValue.getValue());
            }
        } finally {
            context.out();
        }
    }

    @Override
    public Nodes.StaticNode visitObject(Nodes.CompiledObject node) {
        var out = new LinkedHashMap<String, Object>();
        for (var entry : node.getEntries()) {
            if (entry instanceof Nodes.CompiledObject.Spread) {
                visitObjectSpreadField((Nodes.CompiledObject.Spread) entry, out);
            } else if (entry instanceof Nodes.CompiledObject.StaticField) {
                visitObjectStaticField((Nodes.CompiledObject.StaticField) entry, out);
            } else {
                visitObjectSimpleField((Nodes.CompiledObject.Field) entry, out);
            }
        }
        return Nodes.StaticNode.of(out);
    }

    private void visitObjectStaticField(Nodes.CompiledObject.StaticField entry, Map<String, Object> out) {
        var fieldKey = entry.getKey();
        var fieldValue = entry.getValue();
        out.put(fieldKey, fieldValue);
    }

    private void visitObjectSimpleField(Nodes.CompiledObject.Field entry, Map<String, Object> out) {
        var fieldKey = entry.getKey();
        var keyNode = fieldKey.accept(this);
        var key = String.valueOf(keyNode.getValue());
        var fieldValue = entry.getValue();
        var val = fieldValue.accept(this);
        out.put(key, val.getValue());
    }

    private void visitObjectSpreadField(Nodes.CompiledObject.Spread spread, Map<String, Object> out) {
        var spreadExpression = spread.getExpression();
        var value = evaluator.evaluate(spreadExpression, context);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("object spread expects a map");
        }
        @SuppressWarnings("unchecked")
        var m = (Map<String, Object>) value;
        out.putAll(m);
    }

    @Override
    public Nodes.StaticNode visitCond(Nodes.CondNode node) {
        var expression = node.getExpression();
        var value = evaluator.evaluate(expression, context);
        if (value != null) {
            return Nodes.StaticNode.ofCondition(value);
        }
        return Nodes.StaticNode.notCondition();
    }

    @Override
    public Nodes.StaticNode visitSpread(Nodes.SpreadNode node) {
        var expression = node.getExpression();
        var value = evaluator.evaluate(expression, context);
        if (value == null) {
            return Nodes.StaticNode.notSpread();
        }
        var out = new ArrayList<>();
        if (value instanceof Iterable<?>) {
            for (var x : (Iterable<?>) value) {
                out.add(x);
            }
        } else if (value.getClass().isArray()) {
            var len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                out.add(Array.get(value, i));
            }
        } else {
            out.add(value);
        }
        return Nodes.StaticNode.ofSpread(out);
    }

    @Override
    public Nodes.StaticNode visitExpression(Nodes.ExpressionNode node) {
        var evaluated = evaluator.evaluate(node.getExpression(), context);
        return Nodes.StaticNode.of(evaluated);
    }

    @Override
    public Nodes.StaticNode visitList(Nodes.ListNode node) {
        var astNodes = node.getAstNodes();
        var out = new ArrayList<>();
        for (var el : astNodes) {
            var res = el.accept(this);
            if (res.isCond()) {
                if (res.getValue() != null) {
                    out.add(res.getValue());
                }
            } else if (res.isSpread()) {
                if (res.getValue() != null) {
                    out.addAll((Collection<?>) res.getValue());
                }
            } else {
                out.add(res.getValue());
            }
        }
        return Nodes.StaticNode.of(out);
    }

}
