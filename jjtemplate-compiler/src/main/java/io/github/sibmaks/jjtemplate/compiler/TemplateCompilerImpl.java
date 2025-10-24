package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.Token;
import io.github.sibmaks.jjtemplate.lexer.TokenType;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;
import io.github.sibmaks.jjtemplate.parser.api.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

public final class TemplateCompilerImpl implements TemplateCompiler {

    private static final Pattern WHOLE_SPREAD = Pattern.compile("^\\{\\{\\.(.*?)}}$", Pattern.DOTALL);
    private static final Pattern WHOLE_COND = Pattern.compile("^\\{\\{\\?(.*?)}}$", Pattern.DOTALL);
    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
    private final TemplateEvaluator templateEvaluator;

    public TemplateCompilerImpl(Locale locale) {
        this.templateEvaluator = new TemplateEvaluator(locale);
    }

    private static Pair<Boolean, Object> getStaticValue(Object value) {
        if (value == null) {
            return new Pair<>(true, null);
        }
        if (value instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) value;
            var resultMap = new LinkedHashMap<String, Object>(nodeMap.size());
            for (var entry : nodeMap.entrySet()) {
                var mapKey = getStaticValue(entry.getKey());
                if (!mapKey.getFirst()) {
                    return new Pair<>(false, value);
                }
                var mapValue = getStaticValue(entry.getValue());
                if (!mapValue.getFirst()) {
                    return new Pair<>(false, value);
                }
                resultMap.put((String) mapKey.getSecond(), mapValue.getSecond());
            }
            return new Pair<>(true, resultMap);
        } else if (value instanceof List<?>) {
            var nodeList = (List<?>) value;
            var resultList = new ArrayList<>(nodeList.size());
            for (var el : nodeList) {
                var staticValue = getStaticValue(el);
                if (!staticValue.getFirst()) {
                    return new Pair<>(false, value);
                }
                resultList.add(staticValue.getSecond());
            }
            return new Pair<>(true, resultList);
        } else if (value.getClass().isArray()) {
            var len = Array.getLength(value);
            var resultList = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                var el = Array.get(value, i);
                var staticValue = getStaticValue(el);
                if (!staticValue.getFirst()) {
                    return new Pair<>(false, value);
                }
                resultList.add(staticValue.getSecond());
            }
            return new Pair<>(true, resultList);
        } else if (value instanceof Expression) {
            if (value instanceof LiteralExpression) {
                var literalExpression = (LiteralExpression) value;
                return new Pair<>(true, literalExpression.value);
            }
            return new Pair<>(false, value);
        } else if (value instanceof Nodes.CompiledObject) {
            var compiledObject = (Nodes.CompiledObject) value;
            var entries = compiledObject.getEntries();
            var resultMap = new LinkedHashMap<String, Object>(entries.size());
            for (var entry : entries) {
                if (entry instanceof Nodes.CompiledObject.Field) {
                    var field = (Nodes.CompiledObject.Field) entry;
                    var staticKey = getStaticValue(field.getKey());
                    if (!staticKey.getFirst()) {
                        return new Pair<>(false, value);
                    }
                    var staticValue = getStaticValue(field.getValue());
                    if (!staticValue.getFirst()) {
                        return new Pair<>(false, value);
                    }
                    resultMap.put((String) staticKey.getSecond(), staticValue.getSecond());
                } else {
                    return new Pair<>(false, value);
                }
            }
            return new Pair<>(true, resultMap);
        }
        return new Pair<>(true, value);
    }

    @Override
    public CompiledTemplate compile(TemplateScript script) {
        var defs = Optional.ofNullable(script.getDefinitions())
                .orElseGet(List::of);
        var template = script.getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("'template' field required");
        }

        var compiledDefs = new ArrayList<Map<String, Object>>();
        for (var d : defs) {
            var def = (Map<String, Object>) d;
            var compiled = new LinkedHashMap<String, Object>();
            for (var e : def.entrySet()) {
                var header = e.getKey();
                var valueSpec = e.getValue();

                // case
                var ch = parseCaseHeader(header);
                if (ch != null) {
                    var defn = compileCase(valueSpec, ch.expr);
                    compiled.put(ch.varName, defn);
                    continue;
                }
                // range
                var rh = parseRangeHeader(header);
                if (rh != null) {
                    var defn = new Nodes.RangeDefinition(rh.item, rh.index, compileExpression(rh.expr), compileNode(valueSpec));
                    compiled.put(rh.varName, defn);
                    continue;
                }
                // explicit
                var varName = parseSimpleHeader(header);
                if (varName != null) {
                    compiled.put(varName, compileNode(valueSpec));
                    continue;
                }
                throw new IllegalArgumentException("Unknown definition header: " + header);
            }
            compiledDefs.add(compiled);
        }

        var compiledTemplate = compileNode(template);
        return new CompiledTemplateImpl(templateEvaluator, compiledDefs, compiledTemplate);
    }

    private Nodes.CaseDefinition compileCase(Object valueSpec, String caseExpression) {
        if (!(valueSpec instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("case definition expects mapping object");
        }
        var branches = new LinkedHashMap<Expression, Object>();
        var caseDefinitionBuilder = Nodes.CaseDefinition.builder();
        @SuppressWarnings("unchecked")
        var valueSpecMap = (Map<String, Object>) valueSpec;
        for (var ce : valueSpecMap.entrySet()) {
            var condKey = ce.getKey();
            if ("else".equals(condKey)) {
                caseDefinitionBuilder.elseNode(compileNode(ce.getValue()));
                continue;
            }
            if ("then".equals(condKey)) {
                caseDefinitionBuilder.thenNode(compileNode(ce.getValue()));
                continue;
            }
            var condition = compileAsExpression(condKey);
            branches.put(condition, compileNode(ce.getValue()));
        }
        return caseDefinitionBuilder
                .switchExpr(compileExpression(caseExpression))
                .branches(branches)
                .build();
    }

    private Object compileNode(Object node) {
        if (node == null) {
            return null;
        }
        if (node instanceof String) {
            return compileString((String) node);
        }
        if (node instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) node;
            return compileObject(nodeMap);
        }
        if (node instanceof List<?>) {
            var nodeList = (List<?>) node;
            var compiled = new ArrayList<>();
            for (var el : nodeList) {
                var compileNode = compileNode(el);
                compiled.add(compileNode);
            }
            return compiled;
        }
        if (node.getClass().isArray()) {
            var compiled = new ArrayList<>();
            var len = Array.getLength(node);
            for (int i = 0; i < len; i++) {
                var el = Array.get(node, i);
                var compileNode = compileNode(el);
                compiled.add(compileNode);
            }
            return compiled;
        }
        return node; // number, boolean
    }

    private Object compileObject(Map<?, ?> nodeMap) {
        var entries = new ArrayList<Nodes.CompiledObject.Entry>();
        var simple = true;
        var simpleObject = new LinkedHashMap<String, Object>();
        for (var e : nodeMap.entrySet()) {
            var rawKey = String.valueOf(e.getKey());
            var ms = WHOLE_SPREAD.matcher(rawKey);
            if (ms.matches()) {
                // object spread key: "{{. expr}}" — value is ignored
                var expression = compileExpression(rawKey);
                entries.add(new Nodes.CompiledObject.Spread(expression));
                simple = false;
                continue;
            }
            var keyCompiled = compileString(rawKey);
            var valCompiled = compileNode(e.getValue());
            if (simple) {
                var staticKey = getStaticValue(keyCompiled);
                if (!staticKey.getFirst()) {
                    simple = false;
                } else {
                    var staticValue = getStaticValue(valCompiled);
                    if (!staticValue.getFirst()) {
                        simple = false;
                    } else {
                        simpleObject.put((String) staticKey.getSecond(), staticValue.getSecond());
                    }
                }
            }
            entries.add(new Nodes.CompiledObject.Field(keyCompiled, valCompiled));
        }
        if (simple) {
            return simpleObject;
        }
        return new Nodes.CompiledObject(entries);
    }

    private Object compileString(String raw) {
        if (raw.startsWith("{{")) {
            var ms = WHOLE_SPREAD.matcher(raw);
            if (ms.matches()) {
                var expression = compileExpression(raw);
                var foldedExpression = tryFoldConstant(expression);
                if (foldedExpression instanceof LiteralExpression) {
                    return ((LiteralExpression) foldedExpression).value;
                }
                return new Nodes.SpreadNode(foldedExpression);
            }
            var mc = WHOLE_COND.matcher(raw);
            if (mc.matches()) {
                var expression = compileExpression(raw);
                var foldedExpression = tryFoldConstant(expression);
                return new Nodes.CondNode(foldedExpression);
            }
        }
        // literal (no tags) — keep as is
        if (!raw.contains("{{")) {
            return raw;
        }
        // generic expression or inline text — parse with parseTemplate (builds concat when TEXT present)
        var lexer = new TemplateLexer(raw);
        var tokens = lexer.tokens();
        var parser = new TemplateParser(tokens);
        var expression = parser.parseTemplate();
        var foldedExpression = tryFoldConstant(expression);
        if (foldedExpression instanceof LiteralExpression) {
            return ((LiteralExpression) foldedExpression).value;
        }
        return foldedExpression;
    }

    private Expression compileAsExpression(String expr) {
        return compileExpression("{{ " + expr + " }}");
    }

    private Expression compileExpression(String expr) {
        var lexer = new TemplateLexer(expr);
        var tokens = lexer.tokens();
        var inner = new ArrayList<Token>();
        var started = false;
        for (var t : tokens) {
            switch (t.type) {
                case OPEN_EXPR:
                case OPEN_COND:
                case OPEN_SPREAD:
                    started = true;
                    break;
                case CLOSE:
                    started = false;
                    break;
                default:
                    if (started && t.type != TokenType.TEXT) {
                        inner.add(t);
                    }
            }
        }
        var parser = new TemplateParser(inner);
        return parser.parseExpression();
    }

    private String parseSimpleHeader(String h) {
        if (VARIABLE_NAME.matcher(h).matches()) {
            return h;
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
        if (!VARIABLE_NAME.matcher(var).matches()) {
            return null;
        }
        var c = new CaseHeader();
        c.varName = var;
        c.expr = "{{ " + expr + " }}";
        return c;
    }

    private RangeHeader parseRangeHeader(String h) {
        var mark = " range ";
        var i = h.indexOf(mark);
        if (i < 0) {
            return null;
        }
        var var = h.substring(0, i).trim();
        var rest = h.substring(i + mark.length()).trim();
        int ofIdx = rest.indexOf(" of ");
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
        if (!VARIABLE_NAME.matcher(var).matches() || !VARIABLE_NAME.matcher(item).matches() || !VARIABLE_NAME.matcher(index).matches()) {
            return null;
        }
        var r = new RangeHeader();
        r.varName = var;
        r.item = item;
        r.index = index;
        r.expr = "{{ " + expr + " }}";
        return r;
    }

    /**
     * Try to evaluate expression at compile time.
     * If expression is pure (no variable access) — it’s folded into a literal value.
     */
    private Expression tryFoldConstant(Expression expr) {
        try {
            // Walk expression tree to check if it depends on variables
            if (!dependsOnContext(expr)) {
                var value = templateEvaluator.evaluate(expr, new Context(Map.of())).getValue();
                return new LiteralExpression(value);
            }
        } catch (Exception ignore) {
            // ignore folding failures (like divide by zero or unknown function)
        }
        return expr; // keep expression as is
    }

    private boolean dependsOnContext(Expression expr) {
        if (expr == null) {
            return false;
        }

        // VariableExpression always depends on context
        if (expr instanceof VariableExpression) {
            return true;
        }

        if (expr instanceof FunctionCallExpression) {
            var call = (FunctionCallExpression) expr;
            for (var a : call.args) {
                if (dependsOnContext(a)) {
                    return true;
                }
            }
            return false;
        }

        if (expr instanceof PipeExpression) {
            var pipe = (PipeExpression) expr;
            if (dependsOnContext(pipe.left)) {
                return true;
            }
            for (var call : pipe.chain) {
                for (var a : call.args) {
                    if (dependsOnContext(a)) {
                        return true;
                    }
                }
            }
            return false;
        }

        if(expr instanceof TernaryExpression) {
            var ternary = (TernaryExpression) expr;
            return dependsOnContext(ternary.condition) ||
                    dependsOnContext(ternary.ifTrue) ||
                    dependsOnContext(ternary.ifFalse);
        }

        // LiteralExpression never depends on context
        return false;
    }

    // --- headers parsing ---
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
