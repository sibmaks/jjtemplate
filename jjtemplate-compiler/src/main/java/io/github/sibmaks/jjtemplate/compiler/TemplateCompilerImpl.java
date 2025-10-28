package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.optimizer.TemplateOptimizer;
import io.github.sibmaks.jjtemplate.compiler.visitor.AstTreeConvertVisitor;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.TemplateLexerException;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;
import io.github.sibmaks.jjtemplate.parser.api.Expression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link TemplateCompiler} interface.
 * <p>
 * Responsible for compiling template scripts into executable AST structures.
 * It handles parsing of template expressions, variable definitions, ranges,
 * and conditional cases, then applies optimization passes to produce a
 * {@link CompiledTemplate} ready for rendering.
 * </p>
 *
 * <p>Main compilation stages:</p>
 * <ol>
 *   <li>Parsing source definitions and the main template into {@link AstNode} trees.</li>
 *   <li>Transforming expressions into AST nodes via {@link AstTreeConvertVisitor}.</li>
 *   <li>Optimizing the AST using {@link TemplateOptimizer} (constant folding, dead-code elimination, etc.).</li>
 *   <li>Building a {@link CompiledTemplateImpl} for efficient runtime rendering.</li>
 * </ol>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class TemplateCompilerImpl implements TemplateCompiler {

    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
    /**
     * Evaluates expressions and performs constant folding during compilation.
     */
    private final TemplateEvaluator templateEvaluator;

    /**
     * Converts parsed expression trees into executable AST nodes.
     */
    private final AstTreeConvertVisitor astTreeConvert;

    public TemplateCompilerImpl(Locale locale) {
        this.templateEvaluator = new TemplateEvaluator(locale);
        this.astTreeConvert = new AstTreeConvertVisitor(templateEvaluator);
    }

    private static Nodes.StaticNode unwrapList(List<AstNode> compiledList) {
        var values = compiledList.stream()
                .map(it -> (Nodes.StaticNode) it)
                .filter(it -> !it.isCond() || it.getValue() != null)
                .map(Nodes.StaticNode::getValue)
                .collect(Collectors.toList());
        return Nodes.StaticNode.of(values);
    }

    private static boolean compileListSpreadNodeLeaf(
            AstNode child,
            List<AstNode> compiledList
    ) {
        var spreadNode = (Nodes.SpreadNode) child;
        var spreadNodeExpression = spreadNode.getExpression();
        if (!(spreadNodeExpression instanceof LiteralExpression)) {
            compiledList.add(child);
            return false;
        }
        var literalExpression = (LiteralExpression) spreadNodeExpression;
        var value = literalExpression.value;
        if (value == null) {
            return true;
        }
        if (value instanceof List<?>) {
            var subList = (List<?>) value;
            compiledList.addAll(
                    subList.stream()
                            .map(Nodes.StaticNode::of)
                            .collect(Collectors.toList())
            );
        } else {
            compiledList.add(Nodes.StaticNode.of(value));
        }
        return true;
    }

    private static boolean compileListCondNodeLeaf(Nodes.CondNode condNode, ArrayList<AstNode> compiledList) {
        var condNodeExpression = condNode.getExpression();
        if (!(condNodeExpression instanceof LiteralExpression)) {
            compiledList.add(new Nodes.CondNode(condNodeExpression));
            return false;
        }
        var literalExpression = (LiteralExpression) condNodeExpression;
        var value = literalExpression.value;
        if (value != null) {
            compiledList.add(Nodes.StaticNode.of(value));
        }
        return true;
    }

    @Override
    public CompiledTemplate compile(TemplateScript script) {
        var defs = Optional.ofNullable(script.getDefinitions())
                .orElseGet(List::of);
        var template = script.getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("'template' field required");
        }

        var compiledDefs = new ArrayList<Map<String, AstNode>>();
        for (var d : defs) {
            var def = (Map<String, Object>) d;
            var compiled = new LinkedHashMap<String, AstNode>();
            for (var e : def.entrySet()) {
                var header = e.getKey();
                var valueSpec = e.getValue();

                // switch
                var ch = parseSwitchHeader(header);
                if (ch != null) {
                    var defn = compileSwitch(valueSpec, ch.expr);
                    compiled.put(ch.varName, defn);
                    continue;
                }
                // range
                var rh = parseRangeHeader(header);
                if (rh != null) {
                    var defn = new Nodes.RangeDefinition(rh.item, rh.index, compileExpression(rh.expr), compileNode(valueSpec));
                    compiled.put(rh.varName, defn);
                } else {
                    // explicit
                    var varName = parseSimpleHeader(header);
                    compiled.put(varName, compileNode(valueSpec));
                }
            }
            compiledDefs.add(compiled);
        }

        var compiledTemplate = compileNode(template);
        var optimizer = new TemplateOptimizer(templateEvaluator);
        var optimized = optimizer.optimize(compiledDefs, compiledTemplate);
        return new CompiledTemplateImpl(templateEvaluator, optimized.getDefinitions(), optimized.getTemplate());
    }

    private Nodes.SwitchDefinition compileSwitch(Object valueSpec, String switchExpression) {
        if (!(valueSpec instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("switch definition expects mapping object");
        }
        var branches = new LinkedHashMap<Expression, AstNode>();
        var definitionBuilder = Nodes.SwitchDefinition.builder();
        @SuppressWarnings("unchecked")
        var valueSpecMap = (Map<String, Object>) valueSpec;
        for (var ce : valueSpecMap.entrySet()) {
            var condKey = ce.getKey();
            var value = ce.getValue();
            var nestedSwitch = parseSwitchHeader(condKey);
            if (nestedSwitch != null) {
                var nested = compileSwitch(value, nestedSwitch.expr);
                if (Keyword.ELSE.eq(nestedSwitch.varName)) {
                    definitionBuilder.elseNode(nested);
                } else if (Keyword.THEN.eq(nestedSwitch.varName)) {
                    definitionBuilder.thenNode(nested);
                } else {
                    branches.put(compileExpression("{{ " + nestedSwitch.varName + " }}"), nested);
                }
                continue;
            }
            var compiledNode = compileNode(value);
            if (Keyword.ELSE.eq(condKey)) {
                definitionBuilder.elseNode(compiledNode);
            } else if (Keyword.THEN.eq(condKey)) {
                definitionBuilder.thenNode(compiledNode);
            } else {
                var condition = compileAsExpression(condKey);
                branches.put(condition, compiledNode);
            }
        }
        return definitionBuilder
                .switchExpr(compileExpression(switchExpression))
                .branches(branches)
                .build();
    }

    private AstNode compileNode(Object node) {
        if (node == null) {
            return Nodes.StaticNode.empty();
        }

        if (node instanceof String) {
            return compileString((String) node);
        }

        if (node instanceof Map<?, ?>) {
            var nodeMap = (Map<?, ?>) node;
            return compileObject(nodeMap);
        }

        if (node instanceof List<?>) {
            return compileListNode((List<?>) node);
        }

        if (node.getClass().isArray()) {
            return compileListNode(Arrays.asList((Object[]) node));
        }

        return Nodes.StaticNode.of(node);
    }

    private AstNode compileListNode(List<?> nodeList) {
        var compiledList = new ArrayList<AstNode>();
        var staticList = true;
        for (var el : nodeList) {
            var child = compileNode(el);
            if (!staticList || child instanceof Nodes.StaticNode) {
                compiledList.add(child);
            } else if (child instanceof Nodes.SpreadNode) {
                staticList = compileListSpreadNodeLeaf(child, compiledList);
            } else if (child instanceof Nodes.CondNode) {
                staticList = compileListCondNodeLeaf((Nodes.CondNode) child, compiledList);
            } else {
                compiledList.add(child);
                staticList = false;
            }
        }
        if (staticList) {
            return unwrapList(compiledList);
        }
        return new Nodes.ListNode(compiledList);
    }

    private AstNode compileObject(Map<?, ?> nodeMap) {
        var entries = new ArrayList<Nodes.CompiledObject.Entry>();
        var staticMap = new LinkedHashMap<String, Object>(nodeMap.size());
        var allStatic = true;

        for (var e : nodeMap.entrySet()) {
            var rawKey = String.valueOf(e.getKey());
            if (rawKey.startsWith("{{.") && rawKey.endsWith("}}")) {
                // object spread key: "{{. expr}}" — value is ignored
                var expression = compileExpression(rawKey);
                entries.add(new Nodes.CompiledObject.Spread(expression));
                allStatic = false;
                continue;
            }

            var compiledKey = compileString(rawKey);
            var compiledVal = compileNode(e.getValue());

            if (allStatic) {
                if (compiledKey instanceof Nodes.StaticNode && compiledVal instanceof Nodes.StaticNode) {
                    var keyValue = ((Nodes.StaticNode) compiledKey).getValue();
                    var valValue = ((Nodes.StaticNode) compiledVal).getValue();
                    staticMap.put(String.valueOf(keyValue), valValue);
                } else {
                    allStatic = false;
                }
            }

            if (compiledKey instanceof Nodes.StaticNode && compiledVal instanceof Nodes.StaticNode) {
                var keyValue = ((Nodes.StaticNode) compiledKey).getValue();
                var stringKey = String.valueOf(keyValue);
                var valValue = ((Nodes.StaticNode) compiledVal).getValue();
                entries.add(new Nodes.CompiledObject.StaticField(stringKey, valValue));
            } else {
                entries.add(new Nodes.CompiledObject.Field(compiledKey, compiledVal));
            }
        }

        if (allStatic) {
            return Nodes.StaticNode.of(staticMap);
        }

        return new Nodes.CompiledObject(entries);
    }

    private AstNode compileString(String raw) {
        if (raw.endsWith("}}")) {
            if (raw.startsWith("{{.")) {
                var expression = compileExpression(raw);
                var astNode = expression.accept(astTreeConvert);
                if (astNode instanceof Nodes.StaticNode) {
                    var value = ((Nodes.StaticNode) astNode).getValue();
                    var literalExpression = new LiteralExpression(value);
                    return new Nodes.SpreadNode(literalExpression);
                }
                var expressionNode = (Nodes.ExpressionNode) astNode;
                return new Nodes.SpreadNode(expressionNode.getExpression());
            }
            if (raw.startsWith("{{?")) {
                var expression = compileExpression(raw);
                return new Nodes.CondNode(expression);
            }
        }
        // generic expression or inline text — parse with parseTemplate (builds concat when TEXT present)
        var lexer = new TemplateLexer(raw);
        var tokens = lexer.tokens();
        var parser = new TemplateParser(tokens);
        try {
            var expression = parser.parseTemplate();
            return expression.accept(astTreeConvert);
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
        throw new IllegalArgumentException("Illegal variable name '" + h + "'");
    }

    private SwitchHeader parseSwitchHeader(String h) {
        var lexem = Keyword.SWITCH.getLexem();
        var i = h.indexOf(" " + lexem + " ");
        if (i < 0) {
            return null;
        }
        var varName = h.substring(0, i).trim();
        var expr = h.substring(i + lexem.length() + 2 /*spaces*/).trim();
        if (!VARIABLE_NAME.matcher(varName).matches()) {
            throw new IllegalArgumentException("Illegal variable name '" + varName + "'");
        }
        var c = new SwitchHeader();
        c.varName = varName;
        c.expr = "{{ " + expr + " }}";
        return c;
    }

    private RangeHeader parseRangeHeader(String h) {
        var mark = " range ";
        var i = h.indexOf(mark);
        if (i < 0) {
            return null;
        }
        var varName = h.substring(0, i).trim();
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
        if (!VARIABLE_NAME.matcher(varName).matches()) {
            throw new IllegalArgumentException("Illegal variable name '" + varName + "'");
        }
        if (!VARIABLE_NAME.matcher(item).matches()) {
            throw new IllegalArgumentException("Illegal variable name '" + item + "'");
        }
        if (!VARIABLE_NAME.matcher(index).matches()) {
            throw new IllegalArgumentException("Illegal variable name '" + index + "'");
        }
        var r = new RangeHeader();
        r.varName = varName;
        r.item = item;
        r.index = index;
        r.expr = "{{ " + expr + " }}";
        return r;
    }

    // --- headers parsing ---
    private static final class SwitchHeader {
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
