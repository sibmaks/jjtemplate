package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.Nodes;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.parser.api.Expression;

import java.lang.reflect.Array;
import java.util.*;

public final class CompiledTemplate {
    private final TemplateEvaluator evaluator = new TemplateEvaluator();

    private final List<Map<String, Object>> compiledDefs;
    private final Object compiledTemplate;

    public CompiledTemplate(List<Map<String, Object>> defs, Object template) {
        this.compiledDefs = defs;
        this.compiledTemplate = template;
    }

    public Object render(Map<String, Object> ctx) {
        var local = new LinkedHashMap<>(ctx);
        evalDefinitions(compiledDefs, local);
        return renderNode(compiledTemplate, local);
    }

    @SuppressWarnings("unchecked")
    private void evalDefinitions(List<Map<String, Object>> defs, Map<String, Object> ctx) {
        for (var def : defs) {
            for (var e : def.entrySet()) {
                var value = e.getValue();
                if (value instanceof Expression) {
                    var expr = (Expression) value;
                    var evaluated = evaluator.evaluate(expr, new Context(ctx));
                    if(!evaluated.isEmpty()) {
                        ctx.put(e.getKey(), evaluated.getValue());
                    }
                } else if (value instanceof Nodes.CaseDefinition) {
                    var cd = (Nodes.CaseDefinition) value;
                    var evaluated = evaluator.evaluate(cd.getSwitchExpr(), new Context(ctx));
                    if(evaluated.isEmpty()) {
                        continue;
                    }
                    var switchVal = evaluated.getValue();
                    Object selected = null;
                    var matched = false;
                    for (var br : cd.getBranches().entrySet()) {
                        var evaluatedItem = evaluator.evaluate(br.getKey(), new Context(ctx));
                        if(evaluatedItem.isEmpty()) {
                            continue;
                        }
                        var keyVal = evaluatedItem.getValue();
                        if (Objects.equals(switchVal, keyVal)) {
                            selected = br.getValue();
                            matched = true;
                            break;
                        }
                    }
                    if (!matched && Boolean.TRUE.equals(switchVal) && cd.getThenNode() != null) {
                        selected = cd.getThenNode();
                        matched = true;
                    }
                    if (!matched) {
                        selected = cd.getElseNode();
                    }
                    if (selected != null) {
                        ctx.put(e.getKey(), renderNode(selected, ctx));
                    }
                } else if (value instanceof Nodes.RangeDefinition) {
                    var rd = (Nodes.RangeDefinition) value;
                    var evaluated = evaluator.evaluate(rd.getSourceExpr(), new Context(ctx));
                    if(evaluated.isEmpty()) {
                        continue;
                    }
                    var source = evaluated.getValue();
                    if (source == null) {
                        ctx.put(e.getKey(), null);
                        continue;
                    }
                    var out = new ArrayList<>();
                    if (source instanceof Iterable<?>) {
                        int i = 0;
                        for (var it : (Iterable<?>) source) {
                            var child = new LinkedHashMap<>(ctx);
                            child.put(rd.getItem(), it);
                            child.put(rd.getIndex(), i++);
                            out.add(renderNode(rd.getBodyNode(), child));
                        }
                    } else if (source.getClass().isArray()) {
                        int len = Array.getLength(source);
                        for (int i = 0; i < len; i++) {
                            var it = Array.get(source, i);
                            var child = new LinkedHashMap<>(ctx);
                            child.put(rd.getItem(), it);
                            child.put(rd.getIndex(), i);
                            out.add(renderNode(rd.getBodyNode(), child));
                        }
                    } else {
                        throw new IllegalArgumentException("range: expression must be iterable or array");
                    }
                    ctx.put(e.getKey(), out);
                } else {
                    ctx.put(e.getKey(), value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object renderNode(Object node, Map<String, Object> ctx) {
        if (node == null) {
            return null;
        }
        if (node instanceof Expression) {
            var expr = (Expression) node;
            var evaluated = evaluator.evaluate(expr, new Context(ctx));
            return evaluated.getValue();
        }
        if (node instanceof Nodes.CompiledObject) {
            var obj = (Nodes.CompiledObject) node;
            return renderObject(obj, ctx);
        }
        if (node instanceof Map<?, ?>) { // fallback for literal objects
            var m = (Map<?, ?>) node;
            var result = new LinkedHashMap<String, Object>();
            for (var e : m.entrySet()) {
                result.put(String.valueOf(e.getKey()), renderNode(e.getValue(), ctx));
            }
            return result;
        }
        if (node instanceof List<?>) {
            var list = (List<?>) node;
            return renderArray(list, ctx);
        }
        if (node.getClass().isArray()) {
            var out = new ArrayList<>();
            var len = Array.getLength(node);
            for (int i = 0; i < len; i++) {
                out.add(renderNode(Array.get(node, i), ctx));
            }
            return out;
        }
        return node; // primitives
    }

    private Map<String, Object> renderObject(Nodes.CompiledObject obj, Map<String, Object> ctx) {
        var out = new LinkedHashMap<String, Object>();
        for (var entry : obj.getEntries()) {
            if (entry instanceof Nodes.CompiledObject.Spread) {
                var spread = (Nodes.CompiledObject.Spread) entry;
                var evaluated = evaluator.evaluate(spread.getExpression(), new Context(ctx));
                if(evaluated.isEmpty()) {
                    continue;
                }
                var v = evaluated.getValue();
                if (v == null) {
                    continue;
                }
                if (!(v instanceof Map)) {
                    throw new IllegalArgumentException("object spread expects a map");
                }
                @SuppressWarnings("unchecked")
                var m = (Map<String, Object>) v;
                out.putAll(m);
                continue;
            }
            var f = (Nodes.CompiledObject.Field) entry;
            String key;
            var k = f.getKey();
            if (k instanceof Expression) {
                var ke = (Expression) k;
                var evaluated = evaluator.evaluate(ke, new Context(ctx));
                if(evaluated.isEmpty()) {
                    continue;
                }
                var kv = evaluated.getValue();
                key = kv == null ? "null" : String.valueOf(kv);
            } else {
                key = String.valueOf(k);
            }
            var val = renderNode(f.getValue(), ctx);
            out.put(key, val);
        }
        return out;
    }

    private List<Object> renderArray(List<?> list, Map<String, Object> ctx) {
        var out = new ArrayList<>();
        for (var el : list) {
            if (el instanceof Nodes.SpreadNode) {
                var sp = (Nodes.SpreadNode) el;
                var evaluated = evaluator.evaluate(sp.getExpression(), new Context(ctx));
                if(evaluated.isEmpty()) {
                    continue;
                }
                var v = evaluated.getValue();
                if (v instanceof Iterable<?>) {
                    for (Object x : (Iterable<?>) v) out.add(x);
                } else if (v != null && v.getClass().isArray()) {
                    int len = Array.getLength(v);
                    for (int i = 0; i < len; i++) out.add(Array.get(v, i));
                } else {
                    out.add(v);
                }
                continue;
            }
            if (el instanceof Nodes.CondNode) {
                var cn = (Nodes.CondNode) el;
                var evaluated = evaluator.evaluate(cn.getExpression(), new Context(ctx));
                if(evaluated.isEmpty()) {
                    continue;
                }
                var v = evaluated.getValue();
                if (v != null) out.add(v);
                continue;
            }
            out.add(renderNode(el, ctx));
        }
        return out;
    }
}