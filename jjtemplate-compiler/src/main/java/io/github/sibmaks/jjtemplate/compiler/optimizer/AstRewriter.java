package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstVisitor;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstVisitorUtils;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.TemplateExecutionVisitor;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.parser.api.Expression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * AST rewriter that performs constant inlining and local folding on the compiled template AST.
 * <p>
 * Walks the tree and replaces expressions that reference known constants with their literal values.
 * Additionally, performs small structural simplifications (e.g., converting all-static objects/lists
 * into {@link Nodes.StaticNode}).
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class AstRewriter implements AstVisitor<AstNode> {
    /**
     * Evaluator used for partial execution when folding nodes (e.g., in {@code range} bodies).
     */
    private final TemplateEvaluator evaluator;

    /**
     * Map of constant bindings used for inlining (variable name → constant value).
     */
    private final Map<String, Object> constants;

    /**
     * Inlines constant references within the given AST node.
     *
     * @param evaluator evaluator to use for partial execution when needed
     * @param node      the AST node to rewrite
     * @param constants constant bindings to inline
     * @return a possibly rewritten AST node with constants inlined
     */
    static AstNode inlineConstants(
            TemplateEvaluator evaluator,
            AstNode node,
            Map<String, Object> constants
    ) {
        if (node == null || constants.isEmpty()) {
            return node;
        }
        var astRewriter = new AstRewriter(evaluator, constants);
        return AstVisitorUtils.dispatch(node, astRewriter);
    }

    private static Optional<AstNode> foldSwitchExpression(
            LiteralExpression switchExpression,
            Map<Expression, AstNode> branches,
            AstNode thenNode,
            AstNode elseNode
    ) {
        var val = switchExpression.value;
        for (var e : branches.entrySet()) {
            var keyExpression = e.getKey();
            if (keyExpression instanceof LiteralExpression) {
                var keyVal = ((LiteralExpression) keyExpression).value;
                if (Objects.equals(val, keyVal)) {
                    return Optional.ofNullable(e.getValue());
                }
            } else {
                return Optional.empty();
            }
        }
        if (Boolean.TRUE.equals(val) && thenNode != null) {
            return Optional.of(thenNode);
        }
        if (elseNode != null) {
            return Optional.of(elseNode);
        }
        return Optional.of(Nodes.StaticNode.empty());
    }

    private Expression inlineExpr(Expression e) {
        return e.accept(new ExpressionInliner(constants, evaluator));
    }

    @Override
    public AstNode visitStatic(Nodes.StaticNode node) {
        return node;
    }

    @Override
    public AstNode visitExpression(Nodes.ExpressionNode node) {
        var inlined = inlineExpr(node.getExpression());
        if (inlined instanceof LiteralExpression) {
            return Nodes.StaticNode.of(((LiteralExpression) inlined).value);
        }
        if (inlined == node.getExpression()) {
            return node;
        }
        return Nodes.ExpressionNode.builder()
                .expression(inlined)
                .build();
    }

    @Override
    public AstNode visitSpread(Nodes.SpreadNode node) {
        var inlined = inlineExpr(node.getExpression());
        if (inlined == node.getExpression()) {
            return node;
        }
        return Nodes.SpreadNode.builder()
                .expression(inlined)
                .build();
    }

    @Override
    public AstNode visitCond(Nodes.CondNode node) {
        var inlined = inlineExpr(node.getExpression());
        if (inlined == node.getExpression()) {
            return node;
        }
        return Nodes.CondNode.builder()
                .expression(inlined)
                .build();
    }

    @Override
    public AstNode visitObject(Nodes.CompiledObject node) {
        var entries = new ArrayList<Nodes.CompiledObject.Entry>(node.getEntries().size());
        var changed = false;
        var allStatic = true;
        var staticMap = new LinkedHashMap<String, Object>();

        for (var entry : node.getEntries()) {
            if (entry instanceof Nodes.CompiledObject.Field) {
                var field = (Nodes.CompiledObject.Field) entry;
                var rawKey = field.getKey();
                var keyNode = AstVisitorUtils.dispatch(rawKey, this);
                var rawValue = field.getValue();
                var valueNode = AstVisitorUtils.dispatch(rawValue, this);
                changed |= (keyNode != rawKey) || (valueNode != rawValue);

                if (allStatic && keyNode instanceof Nodes.StaticNode && valueNode instanceof Nodes.StaticNode) {
                    var keyStaticNode = (Nodes.StaticNode) keyNode;
                    var keyVal = keyStaticNode.getValue();
                    var valueStaticNode = (Nodes.StaticNode) valueNode;
                    var valVal = valueStaticNode.getValue();
                    staticMap.put(String.valueOf(keyVal), valVal);
                } else {
                    allStatic = false;
                }

                entries.add(
                        Nodes.CompiledObject.Field.builder()
                                .key(keyNode)
                                .value(valueNode)
                                .build()
                );
            } else if (entry instanceof Nodes.CompiledObject.Spread) {
                allStatic = false;
                var s = (Nodes.CompiledObject.Spread) entry;
                var inlined = inlineExpr(s.getExpression());
                changed |= (inlined != s.getExpression());
                entries.add(
                        Nodes.CompiledObject.Spread.builder()
                                .expression(inlined)
                                .build()
                );
            }
        }

        if (allStatic) {
            return Nodes.StaticNode.of(staticMap);
        }

        if (!changed) {
            return node;
        }
        return Nodes.CompiledObject.builder()
                .entries(entries)
                .build();
    }

    @Override
    public AstNode visitSwitch(Nodes.SwitchDefinition node) {
        var changed = false;

        var rawSwitchExpression = node.getSwitchExpr();
        var switchExpression = inlineExpr(rawSwitchExpression);
        changed |= (switchExpression != rawSwitchExpression);

        var branches = new LinkedHashMap<Expression, AstNode>();
        for (var e : node.getBranches().entrySet()) {
            var rawKey = e.getKey();
            var inlinedKey = inlineExpr(rawKey);
            var rawValue = e.getValue();
            var inlinedValue = AstVisitorUtils.dispatch(rawValue, this);
            branches.put(inlinedKey, inlinedValue);
            changed |= (inlinedKey != rawKey) || (inlinedValue != rawValue);
        }

        var rawThenNode = node.getThenNode();
        var thenNode = rawThenNode == null ? null : AstVisitorUtils.dispatch(rawThenNode, this);
        changed |= (thenNode != rawThenNode);

        var rawElseNode = node.getElseNode();
        var elseNode = rawElseNode == null ? null : AstVisitorUtils.dispatch(rawElseNode, this);
        changed |= (elseNode != rawElseNode);

        if (switchExpression instanceof LiteralExpression) {
            var folded = foldSwitchExpression((LiteralExpression) switchExpression, branches, thenNode, elseNode);
            if(folded.isPresent()) {
                return folded.get();
            }
        }

        if (!changed) {
            return node;
        }
        return Nodes.SwitchDefinition.builder()
                .switchExpr(switchExpression)
                .branches(branches)
                .thenNode(thenNode)
                .elseNode(elseNode)
                .build();
    }

    @Override
    public AstNode visitRange(Nodes.RangeDefinition node) {
        var src = inlineExpr(node.getSourceExpr());
        var body = AstVisitorUtils.dispatch(node.getBodyNode(), this);
        if (src == node.getSourceExpr() && body == node.getBodyNode()) {
            return tryFoldRange(node, src, body)
                    .orElse(node);
        }

        var updated = Nodes.RangeDefinition.builder()
                .item(node.getItem())
                .index(node.getIndex())
                .sourceExpr(src)
                .bodyNode(body)
                .build();
        return tryFoldRange(updated, src, body)
                .orElse(updated);
    }

    private Optional<AstNode> tryFoldRange(Nodes.RangeDefinition node, Expression src, AstNode body) {
        if (!(src instanceof LiteralExpression)) {
            return Optional.empty();
        }
        var literalExpression = (LiteralExpression) src;
        var value = literalExpression.value;
        if (value == null) {
            return Optional.empty();
        }

        var refs = AstVarRefCollector.collect(body);
        refs.remove(node.getItem());
        refs.remove(node.getIndex());
        if (!refs.isEmpty()) {
            return Optional.empty();
        }

        var out = new ArrayList<>();
        if (value instanceof Iterable<?>) {
            var i = 0;
            for (var it : (Iterable<?>) value) {
                var localContext = new LinkedHashMap<String, Object>();
                localContext.put(node.getItem(), it); // nullable
                localContext.put(node.getIndex(), i++);
                var templateExecution = new TemplateExecutionVisitor(evaluator, localContext);
                var folded = body.accept(templateExecution);
                if (!folded.isEmpty()) {
                    out.add(folded.getValue());
                }
            }
        } else {
            return Optional.empty();
        }

        return Optional.of(Nodes.StaticNode.of(out));
    }

    @Override
    public AstNode visitList(List<AstNode> node) {
        var changed = false;
        var out = new ArrayList<AstNode>(node.size());
        for (var it : node) {
            var newItem = AstVisitorUtils.dispatch(it, this);
            out.add(newItem);
            changed |= (newItem != it);
        }
        if (!changed) {
            return new Nodes.ListNode(node);
        }
        return new Nodes.ListNode(out);
    }

    @Override
    public AstNode visitDefault(Object node) {
        if (node instanceof AstNode) {
            return (AstNode) node;
        }
        return Nodes.StaticNode.of(node);
    }
}