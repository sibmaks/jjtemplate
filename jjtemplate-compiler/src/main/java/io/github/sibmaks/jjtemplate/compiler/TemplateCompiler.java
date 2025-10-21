package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import io.github.sibmaks.jjtemplate.parser.TemplateParser;
import io.github.sibmaks.jjtemplate.parser.api.Expression;

import java.lang.reflect.Array;
import java.util.*;

public final class TemplateCompiler {

    @SuppressWarnings("unchecked")
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
                compiled.put(e.getKey(), compileNode(e.getValue()));
            }
            compiledDefs.add(compiled);
        }

        var compiledTemplate = compileNode(template);
        return new CompiledTemplate(compiledDefs, compiledTemplate);
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
            var compiled = new LinkedHashMap<String, Object>();
            for (var e : nodeMap.entrySet()) {
                var compileNode = compileNode(e.getValue());
                compiled.put(e.getKey().toString(), compileNode);
            }
            return compiled;
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

    private Expression compileString(String rawExpression) {
        var lexer = new TemplateLexer(rawExpression);
        var tokens = lexer.tokens();
        var parser = new TemplateParser(tokens);
        return parser.parseTemplate();
    }

    public static void main(String[] args) {
        var compiler = new TemplateCompiler();
        var definition = new Definition();
        definition.put("var1", List.of(true, 42));
        var script = new TemplateScript(
                List.of(
                        definition
                ),
                List.of("{{. .var1 }}")
        );
        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        System.out.println(compiled);
        System.out.println(rendered);
    }
}
