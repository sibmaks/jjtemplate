package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.Nodes;
import io.github.sibmaks.jjtemplate.compiler.visitor.ast.AstNode;
import io.github.sibmaks.jjtemplate.evaluator.TemplateEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs optimization passes on the compiled template AST and its definitions.
 * <p>
 * The optimizer repeatedly applies constant inlining, dead-definition elimination,
 * and reachability analysis until no further changes occur.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
public final class TemplateOptimizer {
    /**
     * Evaluator used for partial execution and constant folding during optimization.
     */
    private final TemplateEvaluator evaluator;


    private static boolean isSameDefinitions(
            List<Map<String, AstNode>> a,
            List<Map<String, AstNode>> b
    ) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            var ma = a.get(i);
            var mb = b.get(i);
            if (ma.size() != mb.size()) {
                return false;
            }
            for (var e : ma.entrySet()) {
                if (mb.get(e.getKey()) != e.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Map<String, AstNode>> deepCopy(List<Map<String, AstNode>> defs) {
        var out = new ArrayList<Map<String, AstNode>>(defs.size());
        for (var m : defs) {
            out.add(new LinkedHashMap<>(m));
        }
        return out;
    }

    private static Map<String, Object> collectConstantDefs(List<Map<String, AstNode>> defs) {
        var constants = new LinkedHashMap<String, Object>();
        for (var layer : defs) {
            for (var e : layer.entrySet()) {
                var node = e.getValue();
                if (node instanceof Nodes.StaticNode) {
                    var staticNode = (Nodes.StaticNode) node;
                    var v = staticNode.getValue();
                    constants.put(e.getKey(), v);
                }
            }
        }
        return constants;
    }

    private static Set<String> computeReachable(
            AstNode template,
            List<Map<String, AstNode>> defs
    ) {
        var deps = new LinkedHashMap<String, Set<String>>();
        for (var layer : defs) {
            for (var e : layer.entrySet()) {
                var var = e.getKey();
                var refs = AstVarRefCollector.collect(e.getValue());
                deps.put(var, refs);
            }
        }

        var reachable = AstVarRefCollector.collect(template);

        var changed = true;
        while (changed) {
            changed = false;
            var add = new LinkedHashSet<String>();
            for (var reached : reachable) {
                var rs = deps.get(reached);
                if (rs != null) {
                    add.addAll(rs);
                }
            }
            var before = reachable.size();
            reachable.addAll(add);
            if (reachable.size() != before) {
                changed = true;
            }
        }
        var allDefined = defs.stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        reachable.retainAll(allDefined);
        return reachable;
    }

    private Map<String, AstNode> inlineMap(
            Map<String, AstNode> map,
            Map<String, Object> constants
    ) {
        var out = new LinkedHashMap<String, AstNode>(map.size());
        for (var e : map.entrySet()) {
            var value = AstRewriter.inlineConstants(evaluator, e.getValue(), constants);
            out.put(e.getKey(), value);
        }
        return out;
    }

    /**
     * Optimizes a compiled template and its definitions by applying:
     * <ul>
     *     <li>Constant inlining</li>
     *     <li>Reachability analysis</li>
     *     <li>Dead variable elimination</li>
     * </ul>
     * <p>
     * The optimization process iterates until a stable state is reached (no further changes).
     * </p>
     *
     * @param defs     the list of definition layers to optimize
     * @param template the root AST node of the compiled template
     * @return a {@link Result} containing optimized definitions and the optimized template
     * @throws NullPointerException if {@code defs} or {@code template} is {@code null}
     */
    public Result optimize(
            List<Map<String, AstNode>> defs,
            AstNode template
    ) {
        Objects.requireNonNull(defs, "defs");
        Objects.requireNonNull(template, "template");

        var currentDefs = deepCopy(defs);
        var currentTemplate = template;

        boolean changed;
        do {
            changed = false;

            var constants = collectConstantDefs(currentDefs);

            var inlinedTemplate = AstRewriter.inlineConstants(evaluator, currentTemplate, constants);
            var inlinedDefs = currentDefs.stream()
                    .map(map -> inlineMap(map, constants))
                    .collect(Collectors.toList());

            if (inlinedTemplate != currentTemplate || !isSameDefinitions(currentDefs, inlinedDefs)) {
                currentTemplate = inlinedTemplate;
                currentDefs = inlinedDefs;
                changed = true;
            }

            var reachable = computeReachable(currentTemplate, currentDefs);
            var cleaned = new ArrayList<Map<String, AstNode>>(currentDefs.size());
            for (var layer : currentDefs) {
                var keep = new LinkedHashMap<String, AstNode>();
                for (var e : layer.entrySet()) {
                    var name = e.getKey();
                    if (reachable.contains(name)) {
                        keep.put(name, e.getValue());
                    } else {
                        log.trace("Remove unused definition: {}", name);
                        changed = true;
                    }
                }
                if (!keep.isEmpty()) {
                    cleaned.add(keep);
                }
            }
            currentDefs = cleaned;

        } while (changed);

        return new Result(currentDefs, currentTemplate);
    }

}



