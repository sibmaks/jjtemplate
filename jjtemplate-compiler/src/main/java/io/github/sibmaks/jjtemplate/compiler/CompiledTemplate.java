package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.evaluator.Context;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import io.github.sibmaks.jjtemplate.parser.api.Expression;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                    ctx.put(e.getKey(), evaluated.getValue());
                } else {
                    ctx.put(e.getKey(), value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object renderNode(Object node, Map<String, Object> ctx) {
        if (node instanceof Expression) {
            var expr = (Expression) node;
            var evaluated = evaluator.evaluate(expr, new Context(ctx));
            return evaluated.getValue();
        }
        if (node instanceof Map<?, ?>) {
            var m = (Map<?, ?>) node;
            var result = new LinkedHashMap<String, Object>();
            for (var e : m.entrySet()) {
                result.put(e.getKey().toString(), renderNode(e.getValue(), ctx));
            }
            return result;
        }
        if (node instanceof List<?>) {
            var list = (List<?>) node;
            var out = new ArrayList<>();
            for (var el : list) {
                out.add(renderNode(el, ctx));
            }
            return out;
        }
        return node;
    }
}
