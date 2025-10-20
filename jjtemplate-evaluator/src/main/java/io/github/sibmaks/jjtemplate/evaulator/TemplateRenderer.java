package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.Token;
import io.github.sibmaks.jjtemplate.lexer.TokenType;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TemplateRenderer: executes `definitions` and renders `template` according to the contract.
 *
 * Features:
 *  - Executes variable definitions in order (explicit, case, range)
 *  - Renders JSON-like templates with substitutions, conditional inserts, and spreads
 *  - Supports inline string interpolation and whole-string expression typing
 *  - Honors TemplateEvaluator.Omit for arrays/objects (skip elements/fields)
 */
public final class TemplateRenderer {

    private static final Pattern TAG_ANY = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern TAG_SIMPLE = Pattern.compile("^\\{\\{(.*?)\\}\\}$", Pattern.DOTALL);
    private static final Pattern TAG_COND_SIMPLE = Pattern.compile("^\\{\\{\\?(.*?)\\}\\}$", Pattern.DOTALL);
    private static final Pattern TAG_SPREAD_SIMPLE = Pattern.compile("^\\{\\{\\.(.*?)\\}\\}$", Pattern.DOTALL);

    private final TemplateEvaluator evaluator = new TemplateEvaluator();

    // === Public API ===
    /**
     * Render entry point. Input is a Map matching the JSON root with keys: definitions (optional) and template (required).
     */
    @SuppressWarnings("unchecked")
    public Object render(Map<String, Object> root) {
        Objects.requireNonNull(root, "root");
        Map<String, Object> ctx = new LinkedHashMap<>();

        Object defs = root.get("definitions");
        if (defs instanceof List) {
            evalDefinitions((List<Object>) defs, ctx);
        }
        Object template = root.get("template");
        return renderNode(template, ctx);
    }

    // === Definitions ===
    @SuppressWarnings("unchecked")
    private void evalDefinitions(List<Object> defs, Map<String, Object> ctx) {
        for (Object defBlock : defs) {
            if (!(defBlock instanceof Map)) continue; // ignore non-object blocks
            Map<String, Object> block = (Map<String, Object>) defBlock;
            for (Map.Entry<String, Object> e : block.entrySet()) {
                String header = e.getKey();
                Object valueSpec = e.getValue();
                // case
                CaseHeader ch = parseCaseHeader(header);
                if (ch != null) {
                    if (!(valueSpec instanceof Map)) throw new RuntimeException("case definition expects mapping object");
                    Object caseVal = evalExpr(ch.expr, ctx);
                    Object selected = null; boolean matched = false; Object elseVal = null; Object thenVal = null;
                    for (Map.Entry<String, Object> ce : ((Map<String, Object>) valueSpec).entrySet()) {
                        String condKey = ce.getKey(); Object rhs = ce.getValue();
                        if ("else".equals(condKey)) { elseVal = rhs; continue; }
                        if ("then".equals(condKey)) { thenVal = rhs; continue; }
                        Object keyVal = evalExpr(condKey, ctx); // key is expression literal/var/etc
                        if (Objects.equals(caseVal, keyVal)) { selected = rhs; matched = true; break; }
                    }
                    if (!matched) {
                        if (Boolean.TRUE.equals(caseVal) && thenVal != null) { selected = thenVal; matched = true; }
                    }
                    if (!matched) selected = elseVal;
                    if (selected != null) {
                        Object v = renderNode(selected, ctx);
                        ctx.put(ch.varName, v);
                    }
                    continue;
                }
                // range
                RangeHeader rh = parseRangeHeader(header);
                if (rh != null) {
                    Object colVal = evalExpr(rh.expr, ctx);
                    if (colVal == null) { ctx.put(rh.varName, null); continue; }
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
                SimpleHeader sh = parseSimpleHeader(header);
                if (sh != null) {
                    Object v = renderNode(valueSpec, ctx);
                    ctx.put(sh.varName, v);
                    continue;
                }
                throw new RuntimeException("Unknown definition header: " + header);
            }
        }
    }

    private static final class SimpleHeader { String varName; }
    private static final class CaseHeader { String varName; String expr; }
    private static final class RangeHeader { String varName; String item; String index; String expr; }

    private SimpleHeader parseSimpleHeader(String h) {
        if (h.matches("[A-Za-z][A-Za-z0-9]*")) { SimpleHeader s = new SimpleHeader(); s.varName = h; return s; }
        return null;
    }

    private CaseHeader parseCaseHeader(String h) {
        int i = h.indexOf(" case ");
        if (i < 0) return null;
        String var = h.substring(0, i).trim();
        String expr = h.substring(i + 6).trim();
        if (!var.matches("[A-Za-z][A-Za-z0-9]*")) return null;
        CaseHeader c = new CaseHeader(); c.varName = var; c.expr = expr; return c;
    }

    private RangeHeader parseRangeHeader(String h) {
        // pattern: varName range item,index of <expr>
        String mark = " range ";
        int i = h.indexOf(mark);
        if (i < 0) return null;
        String var = h.substring(0, i).trim();
        String rest = h.substring(i + mark.length()).trim();
        int ofIdx = rest.indexOf(" of ");
        if (ofIdx < 0) return null;
        String vars = rest.substring(0, ofIdx).trim();
        String expr = rest.substring(ofIdx + 4).trim();
        String[] parts = vars.split(",");
        if (parts.length != 2) return null;
        String item = parts[0].trim();
        String index = parts[1].trim();
        if (!var.matches("[A-Za-z][A-Za-z0-9]*") || !item.matches("[A-Za-z][A-Za-z0-9]*") || !index.matches("[A-Za-z][A-Za-z0-9]*")) return null;
        RangeHeader r = new RangeHeader(); r.varName = var; r.item = item; r.index = index; r.expr = expr; return r;
    }

    // === Rendering nodes ===
    @SuppressWarnings("unchecked")
    private Object renderNode(Object node, Map<String, Object> ctx) {
        if (node == null) return null;
        if (node instanceof String) return renderString((String) node, ctx);
        if (node instanceof Number || node instanceof Boolean) return node; // primitives as is
        if (node instanceof List) return renderArray((List<Object>) node, ctx);
        if (node instanceof Map) return renderObject((Map<String, Object>) node, ctx);
        // Unexpected type in input tree – return as is
        return node;
    }

    private Object renderString(String s, Map<String, Object> ctx) {
        Matcher mSpread = TAG_SPREAD_SIMPLE.matcher(s.trim());
        if (mSpread.matches()) {
            // callers (array/object) handle semantics; here we just evaluate and return
            return evalExpr(mSpread.group(1), ctx);
        }
        Matcher mCond = TAG_COND_SIMPLE.matcher(s.trim());
        if (mCond.matches()) {
            return evalExpr(mCond.group(1), ctx); // caller decides to include/skip when used in arrays
        }
        Matcher mWhole = TAG_SIMPLE.matcher(s.trim());
        if (mWhole.matches()) {
            return evalExpr(mWhole.group(1), ctx); // whole-string replacement keeps type
        }
        // inline interpolation(s)
        StringBuilder out = new StringBuilder();
        int last = 0; Matcher m = TAG_ANY.matcher(s);
        while (m.find()) {
            out.append(s, last, m.start());
            String inner = m.group(1);
            // remove optional leading '?' or '.' for inline; treat as normal eval
            inner = inner.trim();
            if (!inner.isEmpty() && (inner.charAt(0) == '?' || inner.charAt(0) == '.')) inner = inner.substring(1).trim();
            Object val = evalExpr(inner, ctx);
            out.append(val);
            last = m.end();
        }
        out.append(s.substring(last));
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    private List<Object> renderArray(List<Object> arr, Map<String, Object> ctx) {
        List<Object> out = new ArrayList<>();
        for (Object el : arr) {
            if (el instanceof String) {
                String s = (String) el;
                String trimmed = s.trim();
                Matcher mCond = TAG_COND_SIMPLE.matcher(trimmed);
                if (mCond.matches()) {
                    Object v = evalExpr(mCond.group(1), ctx);
                    if (v != null) out.add(v);
                    continue;
                }
                Matcher mSpread = TAG_SPREAD_SIMPLE.matcher(trimmed);
                if (mSpread.matches()) {
                    Object v = evalExpr(mSpread.group(1), ctx);
                    if (v instanceof Iterable) for (Object x : (Iterable<Object>) v) out.add(x);
                    else if (v != null && v.getClass().isArray()) {
                        int len = java.lang.reflect.Array.getLength(v);
                        for (int i = 0; i < len; i++) out.add(java.lang.reflect.Array.get(v, i));
                    } else {
                        out.add(v);
                    }
                    continue;
                }
                Matcher mWhole = TAG_SIMPLE.matcher(trimmed);
                if (mWhole.matches()) {
                    Object v = evalExpr(mWhole.group(1), ctx);
                    if (v != Omit.INSTANCE) out.add(v);
                    continue;
                }
            }
            Object v = renderNode(el, ctx);
            if (v != Omit.INSTANCE) out.add(v);
        }
        return out;
    }

    private Map<String, Object> renderObject(Map<String, Object> obj, Map<String, Object> ctx) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : obj.entrySet()) {
            String rawKey = e.getKey();
            String trimmed = rawKey.trim();
            Matcher mSpread = TAG_SPREAD_SIMPLE.matcher(trimmed);
            if (mSpread.matches()) {
                Object v = evalExpr(mSpread.group(1), ctx);
                if (!(v instanceof Map)) throw new RuntimeException("object spread expects a map");
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) v;
                out.putAll(m); // override existing keys
                continue;
            }
            // normal key: interpolate strings
            String key = (String) renderString(rawKey, ctx);
            Object val = renderNode(e.getValue(), ctx);
            if (val != Omit.INSTANCE) out.put(key, val);
        }
        return out;
    }

    // === Expression evaluation helper ===
    private Object evalExpr(String expr, Map<String, Object> ctx) {
        expr = expr == null ? "" : expr;
        var wrapped = "{{" + expr + "}}";
        var lexer = new TemplateLexer(wrapped);
        var toks = lexer.tokens();
        // take tokens between OPEN_* and CLOSE
        var inner = new ArrayList<Token>();
        var started = false;
        for (var t : toks) {
            switch (t.type) {
                case OPEN_EXPR: case OPEN_COND: case OPEN_SPREAD: started = true; break;
                case CLOSE: started = false; break;
                default:
                    if (started && t.type != TokenType.TEXT) inner.add(t);
            }
        }
        var parser = new TemplateParser(inner);
        var ast = parser.parseExpression();
        return evaluator.evaluate(ast, new Context(ctx)).getValue();
    }

    // --- Simple demo ---
    public static void main(String[] args) {
        TemplateRenderer r = new TemplateRenderer();
        Map<String, Object> root = new LinkedHashMap<>();
        // definitions
        List<Object> defs = new ArrayList<>();
        Map<String, Object> d1 = new LinkedHashMap<>();
        d1.put("a", 1);
        d1.put("b", 2);
        d1.put("sum", "{{ concat .a, .b | int }}"); // -> "12" | int => 12
        Map<String, Object> d2 = new LinkedHashMap<>();
        Map<String, Object> caseMap = new LinkedHashMap<>();
        caseMap.put("42", "forty-two");
        caseMap.put("true", "yes");
        caseMap.put("else", "other");
        d2.put("result case .sum", caseMap);
        defs.add(d1); defs.add(d2);
        root.put("definitions", defs);
        // template
        Map<String, Object> tpl = new LinkedHashMap<>();
        tpl.put("booleanTrue", "{{ true }}");
        tpl.put("key-{{ 'end' }}", "value");
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("x", 1);
        obj.put("{{. {\\\"y\\\": 2} }}", true); // demo: spread inline literal map
        tpl.put("object", obj);
        List<Object> arr = new ArrayList<>();
        arr.add("prefix");
        arr.add("{{? .sum }}"); // inserts 12
        arr.add("{{. ['a','b'] }}"); // spreads
        tpl.put("array", arr);
        root.put("template", tpl);

        Object rendered = r.render(root);
        System.out.println(rendered);
    }
}
