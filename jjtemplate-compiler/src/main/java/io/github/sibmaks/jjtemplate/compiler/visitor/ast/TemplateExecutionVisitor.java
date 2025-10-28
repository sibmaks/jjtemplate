package io.github.sibmaks.jjtemplate.compiler.visitor.ast;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import lombok.AllArgsConstructor;

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
@AllArgsConstructor
public class TemplateExecutionVisitor implements AstVisitor<Nodes.StaticNode> {
    /**
     * Evaluator used to compute the values of expressions within the template.
     */
    private final TemplateEvaluator evaluator;

    /**
     * The current variable context used during template execution.
     */
    private final Map<String, Object> context;


    @Override
    public Nodes.StaticNode visitStatic(Nodes.StaticNode node) {
        return node;
    }

    @Override
    public Nodes.StaticNode visitSwitch(Nodes.SwitchDefinition node) {
        var localContext = new Context(context);
        var evaluated = evaluator.evaluate(node.getSwitchExpr(), localContext);
        if (evaluated.isEmpty()) {
            return Nodes.StaticNode.empty();
        }
        var switchVal = evaluated.getValue();
        AstNode selected = null;
        var matched = false;
        var branches = node.getBranches();
        for (var branch : branches.entrySet()) {
            var evaluatedItem = evaluator.evaluate(branch.getKey(), localContext);
            if (!evaluatedItem.isEmpty()) {
                var keyVal = evaluatedItem.getValue();
                if (Objects.equals(switchVal, keyVal)) {
                    selected = branch.getValue();
                    matched = true;
                    break;
                }
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
        var evaluated = evaluator.evaluate(node.getSourceExpr(), new Context(context));
        if (evaluated.isEmpty()) {
            return Nodes.StaticNode.empty();
        }
        var source = evaluated.getValue();
        if (source == null) {
            return Nodes.StaticNode.empty();
        }
        var out = new ArrayList<>();
        if (source instanceof Iterable<?>) {
            int i = 0;
            for (var it : (Iterable<?>) source) {
                var child = new LinkedHashMap<>(context);
                child.put(node.getItem(), it);
                child.put(node.getIndex(), i++);
                var bodyNode = node.getBodyNode();
                var executor = new TemplateExecutionVisitor(evaluator, child);
                var selectedValue = bodyNode.accept(executor);
                if (!selectedValue.isEmpty()) {
                    out.add(selectedValue.getValue());
                }
            }
        } else {
            throw new IllegalArgumentException("range: expression must be iterable or array");
        }
        return Nodes.StaticNode.of(out);
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
        var evaluated = evaluator.evaluate(spreadExpression, new Context(context));
        if (evaluated.isEmpty()) {
            return;
        }
        var value = evaluated.getValue();
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
        var evaluated = evaluator.evaluate(expression, new Context(context));
        if (evaluated.isEmpty()) {
            return Nodes.StaticNode.notCondition();
        }
        var value = evaluated.getValue();
        if (value != null) {
            return Nodes.StaticNode.ofCondition(value);
        }
        return Nodes.StaticNode.notCondition();
    }

    @Override
    public Nodes.StaticNode visitSpread(Nodes.SpreadNode node) {
        var expression = node.getExpression();
        var evaluated = evaluator.evaluate(expression, new Context(context));
        if (evaluated.isEmpty()) {
            return Nodes.StaticNode.notSpread();
        }
        var out = new ArrayList<>();
        var v = evaluated.getValue();
        if (v instanceof Iterable<?>) {
            for (var x : (Iterable<?>) v) {
                out.add(x);
            }
        } else if (v != null && v.getClass().isArray()) {
            var len = Array.getLength(v);
            for (int i = 0; i < len; i++) {
                out.add(Array.get(v, i));
            }
        } else {
            out.add(v);
        }
        return Nodes.StaticNode.ofSpread(out);
    }

    @Override
    public Nodes.StaticNode visitExpression(Nodes.ExpressionNode node) {
        var evaluated = evaluator.evaluate(node.getExpression(), new Context(context));
        return Nodes.StaticNode.of(evaluated.getValue());
    }

    @Override
    public Nodes.StaticNode visitList(List<AstNode> node) {
        var out = new ArrayList<>();
        for (var el : node) {
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

    @Override
    public Nodes.StaticNode visitDefault(Object node) {
        return Nodes.StaticNode.empty();
    }

}
