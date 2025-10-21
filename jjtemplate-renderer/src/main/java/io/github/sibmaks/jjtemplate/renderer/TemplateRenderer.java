package io.github.sibmaks.jjtemplate.renderer;

import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;

import java.util.*;
import java.util.regex.Pattern;

/**
 * TemplateRenderer: executes `definitions` and renders `template` according to the contract.
 * <p>
 * Features:
 * - Executes variable definitions in order (explicit, case, range)
 * - Renders JSON-like templates with substitutions, conditional inserts, and spreads
 * - Supports inline string interpolation and whole-string expression typing
 * - Honors TemplateEvaluator.Omit for arrays/objects (skip elements/fields)
 */
public final class TemplateRenderer {

    private static final Pattern TAG_COND_SIMPLE = Pattern.compile("^\\{\\{\\?(.*?)\\}\\}$", Pattern.DOTALL);
    private static final Pattern TAG_SPREAD_SIMPLE = Pattern.compile("^\\{\\{\\.(.*?)\\}\\}$", Pattern.DOTALL);

    private final TemplateEvaluator evaluator = new TemplateEvaluator();

    // === Public API ===

    /**
     * Render entry point. Input is a Map matching the JSON root with keys: definitions (optional) and template (required).
     */
    @SuppressWarnings("unchecked")
    public Object render(
            Map<String, Object> root,
            Map<String, Object> context
    ) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(context, "context");
        var mutableContext = new LinkedHashMap<>(context);

        var defs = root.get("definitions");
        if (defs instanceof List) {
            evalDefinitions((List<Object>) defs, mutableContext);
        }
        var template = root.get("template");
        return renderNode(template, mutableContext);
    }

    /**
     * Render entry point. Input is a Map matching the JSON root with keys: definitions (optional) and template (required).
     */
    @SuppressWarnings("unchecked")
    public Object render(Map<String, Object> root) {
        return render(
                root,
                new LinkedHashMap<>()
        );
    }

    // === Definitions ===
    @SuppressWarnings("unchecked")
    private void evalDefinitions(List<Object> defs, Map<String, Object> ctx) {
        for (Object defBlock : defs) {
            if (!(defBlock instanceof Map)) {
                continue; // ignore non-object blocks
            }
            var block = (Map<String, Object>) defBlock;
            for (var e : block.entrySet()) {
                var header = e.getKey();
                var valueSpec = e.getValue();
                // case
                var ch = parseCaseHeader(header);
                if (ch != null) {
                    if (!(valueSpec instanceof Map)) {
                        throw new RuntimeException("case definition expects mapping object");
                    }
                    var caseVal = evalExpr(ch.expr, ctx);
                    Object selected = null;
                    var matched = false;
                    Object elseVal = null;
                    Object thenVal = null;
                    for (var ce : ((Map<String, Object>) valueSpec).entrySet()) {
                        String condKey = ce.getKey();
                        Object rhs = ce.getValue();
                        if ("else".equals(condKey)) {
                            elseVal = rhs;
                            continue;
                        }
                        if ("then".equals(condKey)) {
                            thenVal = rhs;
                            continue;
                        }
                        var keyVal = evalExpr("{{ " + condKey + " }}", ctx); // key is expression literal/var/etc
                        if (Objects.equals(caseVal.getValue(), keyVal.getValue())) {
                            selected = rhs;
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        if (Boolean.TRUE.equals(caseVal.getValue()) && thenVal != null) {
                            selected = thenVal;
                            matched = true;
                        }
                    }
                    if (!matched) selected = elseVal;
                    if (selected != null) {
                        Object v = renderNode(selected, ctx);
                        ctx.put(ch.varName, v);
                    }
                    continue;
                }
                // range
                var rh = parseRangeHeader(header);
                if (rh != null) {
                    var colValExpr = evalExpr(rh.expr, ctx);
                    var colVal = colValExpr.getValue();
                    if (colVal == null) {
                        ctx.put(rh.varName, null);
                        continue;
                    }
                    List<Object> out = new ArrayList<>();
                    if (colVal instanceof Iterable) {
                        int idx = 0;
                        for (Object it : (Iterable<?>) colVal) {
                            Map<String, Object> child = new LinkedHashMap<>(ctx);
                            child.put(rh.item, it);
                            child.put(rh.index, idx++);
                            out.add(renderNode(e.getValue(), child));
                        }
                    } else if (colVal.getClass().isArray()) {
                        int len = java.lang.reflect.Array.getLength(colVal);
                        for (int i = 0; i < len; i++) {
                            Object it = java.lang.reflect.Array.get(colVal, i);
                            Map<String, Object> child = new LinkedHashMap<>(ctx);
                            child.put(rh.item, it);
                            child.put(rh.index, i);
                            out.add(renderNode(e.getValue(), child));
                        }
                    } else {
                        throw new RuntimeException("range: expression must be iterable or array");
                    }
                    ctx.put(rh.varName, out);
                    continue;
                }
                // explicit var
                var sh = parseSimpleHeader(header);
                if (sh != null) {
                    Object v = renderNode(valueSpec, ctx);
                    ctx.put(sh.varName, v);
                    continue;
                }
                throw new RuntimeException("Unknown definition header: " + header);
            }
        }
    }

    private SimpleHeader parseSimpleHeader(String h) {
        if (h.matches("[A-Za-z][A-Za-z0-9]*")) {
            var s = new SimpleHeader();
            s.varName = h;
            return s;
        }
        return null;
    }

    private CaseHeader parseCaseHeader(String h) {
        var i = h.indexOf(" case ");
        if (i < 0) {
            return null;
        }
        var var = h.substring(0, i).trim();
        var expr = h.substring(i + 6).trim();
        if (!var.matches("[A-Za-z][A-Za-z0-9]*")) {
            return null;
        }
        var c = new CaseHeader();
        c.varName = var;
        c.expr = "{{ " + expr + " }}";
        return c;
    }

    private RangeHeader parseRangeHeader(String h) {
        // pattern: varName range item,index of <expr>
        var mark = " range ";
        var i = h.indexOf(mark);
        if (i < 0) {
            return null;
        }
        var var = h.substring(0, i).trim();
        var rest = h.substring(i + mark.length()).trim();
        var ofIdx = rest.indexOf(" of ");
        if (ofIdx < 0) {
            return null;
        }
        var vars = rest.substring(0, ofIdx).trim();
        var expr = rest.substring(ofIdx + 4).trim();
        var parts = vars.split(",");
        if (parts.length != 2) {
            return null;
        }
        var item = parts[0].trim();
        var index = parts[1].trim();
        if (!var.matches("[A-Za-z][A-Za-z0-9]*") || !item.matches("[A-Za-z][A-Za-z0-9]*") || !index.matches("[A-Za-z][A-Za-z0-9]*")) {
            return null;
        }
        var r = new RangeHeader();
        r.varName = var;
        r.item = item;
        r.index = index;
        r.expr = "{{ " + expr + " }}";
        return r;
    }

    // === Rendering nodes ===
    @SuppressWarnings("unchecked")
    private Object renderNode(Object node, Map<String, Object> ctx) {
        if (node == null) return null;
        if (node instanceof String) return evalExpr((String) node, ctx).getValue();
        if (node instanceof Number || node instanceof Boolean) return node; // primitives as is
        if (node instanceof List) return renderArray((List<Object>) node, ctx);
        if (node instanceof Map) return renderObject((Map<String, Object>) node, ctx);
        // Unexpected type in input tree – return as is
        return node;
    }

    @SuppressWarnings("unchecked")
    private List<Object> renderArray(List<Object> arr, Map<String, Object> ctx) {
        var out = new ArrayList<>();
        for (var el : arr) {
            if (el instanceof String) {
                var s = (String) el;
                var trimmed = s.trim();
                var mCond = TAG_COND_SIMPLE.matcher(trimmed);
                if (mCond.matches()) {
                    var v = evalExpr(trimmed, ctx).getValue();
                    if (v != null) out.add(v);
                    continue;
                }
                var mSpread = TAG_SPREAD_SIMPLE.matcher(trimmed);
                if (mSpread.matches()) {
                    var v = evalExpr(trimmed, ctx).getValue();
                    if (v instanceof Iterable) {
                        for (var x : (Iterable<Object>) v) {
                            out.add(x);
                        }
                    } else if (v != null && v.getClass().isArray()) {
                        var len = java.lang.reflect.Array.getLength(v);
                        for (int i = 0; i < len; i++) {
                            out.add(java.lang.reflect.Array.get(v, i));
                        }
                    } else {
                        out.add(v);
                    }
                    continue;
                }
                var v = evalExpr(trimmed, ctx);
                if (!v.isEmpty()) {
                    out.add(v.getValue());
                }
                continue;
            }
            var v = renderNode(el, ctx);
            out.add(v);
        }
        return out;
    }

    private Map<String, Object> renderObject(Map<String, Object> obj, Map<String, Object> ctx) {
        var out = new LinkedHashMap<String, Object>();
        for (var e : obj.entrySet()) {
            var rawKey = e.getKey();
            var trimmed = rawKey.trim();
            var mSpread = TAG_SPREAD_SIMPLE.matcher(trimmed);
            if (mSpread.matches()) {
                var v = evalExpr(trimmed, ctx);
                if(v.isEmpty()) {
                    continue;
                }
                var value = v.getValue();
                if (!(value instanceof Map)) {
                    throw new RuntimeException("object spread expects a map");
                }
                @SuppressWarnings("unchecked")
                var m = (Map<String, Object>) value;
                out.putAll(m); // override existing keys
                continue;
            }
            // normal key: interpolate strings
            var key = (String) evalExpr(rawKey, ctx).getValue();
            var val = renderNode(e.getValue(), ctx);
            out.put(key, val);
        }
        return out;
    }

    // === Expression evaluation helper ===
    private ExpressionValue evalExpr(String expr, Map<String, Object> ctx) {
        expr = expr == null ? "" : expr;
        var wrapped = expr;
        var lexer = new TemplateLexer(wrapped);
        var tokens = lexer.tokens();
        var parser = new TemplateParser(tokens);
        var ast = parser.parseTemplate();
        return evaluator.evaluate(ast, new Context(ctx));
    }

    private static final class SimpleHeader {
        String varName;
    }

    private static final class CaseHeader {
        String varName;
        String expr;
    }

    private static final class RangeHeader {
        String varName;
        String item;
        String index;
        String expr;
    }
}
