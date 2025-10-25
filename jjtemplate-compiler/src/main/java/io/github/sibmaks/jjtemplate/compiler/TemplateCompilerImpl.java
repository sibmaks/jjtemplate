package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.lexer.*;
import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.TemplateLexerException;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
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

    private static Object unwrapStatic(Object node) {
        if (node instanceof Nodes.StaticNode) {
            var staticNode = (Nodes.StaticNode) node;
            return staticNode.getValue();
        }
        if (node instanceof LiteralExpression) {
            var literalExpression = (LiteralExpression) node;
            return literalExpression.value;
        }
        if (node instanceof List<?>) {
            var nodeList = (List<?>) node;
            var result = new ArrayList<>();
            for (var el : nodeList) {
                result.add(unwrapStatic(el));
            }
            return result;
        }
        if (node instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) node;
            var result = new LinkedHashMap<>();
            for (var e : nodeMap.entrySet()) {
                result.put(unwrapStatic(e.getKey()), unwrapStatic(e.getValue()));
            }
            return result;
        }
        return node;
    }

    private boolean isStatic(Object node) {
        if (node == null) {
            return true;
        }
        if (node instanceof LiteralExpression) {
            return true;
        }
        if(node instanceof Expression) {
            return false;
        }
        if (node instanceof Nodes.StaticNode) {
            return true;
        }
        if (node instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) node;
            for (var e : nodeMap.entrySet()) {
                if (!isStatic(e.getKey()) || !isStatic(e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        if (node instanceof List<?>) {
            var nodeList = (List<?>) node;
            for (var el : nodeList) {
                if (!isStatic(el)) {
                    return false;
                }
            }
            return true;
        }
        if (node instanceof Nodes.CompiledObject) {
            var compiledObject = (Nodes.CompiledObject) node;
            for (var entry : compiledObject.getEntries()) {
                if (entry instanceof Nodes.CompiledObject.Field) {
                    var field = (Nodes.CompiledObject.Field) entry;
                    if (!isStatic(field.getKey()) || !isStatic(field.getValue())) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        if(node instanceof Nodes.SpreadNode) {
            return false;
        }
        return !(node instanceof Nodes.CondNode);
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
            if (Keyword.ELSE.eq(condKey)) {
                caseDefinitionBuilder.elseNode(compileNode(ce.getValue()));
                continue;
            }
            if (Keyword.THEN.eq(condKey)) {
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
            return new Nodes.StaticNode(null);
        }

        if (node instanceof String) {
            var compiled = compileString((String) node);
            if (isStatic(compiled)) {
                return new Nodes.StaticNode(unwrapStatic(compiled));
            }
            return compiled;
        }

        if (node instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) node;
            var compiled = compileObject(nodeMap);
            if (isStatic(compiled)) {
                return new Nodes.StaticNode(unwrapStatic(compiled));
            }
            return compiled;
        }

        if (node instanceof List<?>) {
            var nodeList = (List<?>) node;
            var compiledList = new ArrayList<>();
            for (var el : nodeList) {
                compiledList.add(compileNode(el));
            }
            if (isStatic(compiledList)) {
                return new Nodes.StaticNode(unwrapStatic(compiledList));
            }
            return compiledList;
        }

        if (node.getClass().isArray()) {
            var compiledList = new ArrayList<>();
            int len = Array.getLength(node);
            for (int i = 0; i < len; i++) {
                compiledList.add(compileNode(Array.get(node, i)));
            }
            if (isStatic(compiledList)) {
                return new Nodes.StaticNode(unwrapStatic(compiledList));
            }
            return compiledList;
        }

        return new Nodes.StaticNode(node);
    }

    private Object compileObject(Map<?, ?> nodeMap) {
        var entries = new ArrayList<Nodes.CompiledObject.Entry>();
        var staticMap = new LinkedHashMap<String, Object>(nodeMap.size());
        var allStatic = true;

        for (var e : nodeMap.entrySet()) {
            var rawKey = String.valueOf(e.getKey());
            var spreadMatcher = WHOLE_SPREAD.matcher(rawKey);
            if (spreadMatcher.matches()) {
                // object spread key: "{{. expr}}" — value is ignored
                var expression = compileExpression(rawKey);
                var folded = tryFoldConstant(expression);
                entries.add(new Nodes.CompiledObject.Spread(folded));
                allStatic = false;
                continue;
            }

            var compiledKey = compileString(rawKey);
            var compiledVal = compileNode(e.getValue());

            if (allStatic && isStatic(compiledKey) && isStatic(compiledVal)) {
                var keyValue = unwrapStatic(compiledKey);
                var valValue = unwrapStatic(compiledVal);
                staticMap.put(String.valueOf(keyValue), valValue);
            } else {
                allStatic = false;
            }

            entries.add(new Nodes.CompiledObject.Field(compiledKey, compiledVal));
        }

        if (allStatic) {
            return new Nodes.StaticNode(staticMap);
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
        try {
            var expression = parser.parseTemplate();
            var foldedExpression = tryFoldConstant(expression);
            if (foldedExpression instanceof LiteralExpression) {
                return ((LiteralExpression) foldedExpression).value;
            }
            return foldedExpression;
        } catch (TemplateLexerException e) {
            throw new IllegalArgumentException("String compilation error: '" + raw + "'", e);
        }
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

        if (expr instanceof TernaryExpression) {
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
