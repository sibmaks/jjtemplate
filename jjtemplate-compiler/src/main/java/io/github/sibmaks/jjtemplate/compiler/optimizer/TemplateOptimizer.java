package io.github.sibmaks.jjtemplate.compiler.optimizer;

import io.github.sibmaks.jjtemplate.compiler.InternalVariable;
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

    private static Map<String, Object> collectConstantDefs(List<InternalVariable> internalVariables) {
        var constants = new HashMap<String, Object>();
        for (var variable : internalVariables) {
            var node = variable.getValue();
            if (node instanceof Nodes.StaticNode) {
                var staticNode = (Nodes.StaticNode) node;
                var v = staticNode.getValue();
                constants.put(variable.getName(), v);
            }
        }
        return constants;
    }

    private static Set<String> computeReachable(
            AstNode template,
            List<InternalVariable> internalVariables
    ) {
        var deps = new HashMap<String, Set<String>>();
        for (var e : internalVariables) {
            var varName = e.getName();
            var refs = AstVarRefCollector.collect(e.getValue());
            deps.put(varName, refs);
        }

        var reachable = AstVarRefCollector.collect(template);

        var changed = true;
        while (changed) {
            changed = false;
            var add = new HashSet<String>();
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
        var allDefined = internalVariables.stream()
                .map(InternalVariable::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        reachable.retainAll(allDefined);
        return reachable;
    }

    private static List<InternalVariable> eliminateUnusedDefs(
            List<InternalVariable> internalVariables,
            Set<String> reachable
    ) {
        var cleaned = new ArrayList<InternalVariable>(internalVariables.size());
        var changed = false;
        for (var variable : internalVariables) {
            var name = variable.getName();
            if (reachable.contains(name)) {
                cleaned.add(variable);
            } else {
                log.trace("Remove unused definition: {}", name);
                changed = true;
            }
        }
        if (!changed) {
            return internalVariables;
        }
        return cleaned;
    }

    private InternalVariable inlineVariable(
            InternalVariable internalVariable,
            Map<String, Object> constants
    ) {
        var value = AstRewriter.inlineConstants(evaluator, internalVariable.getValue(), constants);
        internalVariable.setValue(value);
        return internalVariable;
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
     * @param internalVariables the list of internal variables to optimize
     * @param template          the root AST node of the compiled template
     * @return a {@link Result} containing optimized definitions and the optimized template
     * @throws NullPointerException if {@code internalVariables} or {@code template} is {@code null}
     */
    public Result optimize(
            List<InternalVariable> internalVariables,
            AstNode template
    ) {
        Objects.requireNonNull(internalVariables, "internalVariables");
        Objects.requireNonNull(template, "template");

        List<InternalVariable> currentDefs = new ArrayList<>(internalVariables);
        var currentTemplate = template;

        boolean changed;
        do {
            changed = false;

            var constants = collectConstantDefs(currentDefs);

            var inlinedTemplate = AstRewriter.inlineConstants(evaluator, currentTemplate, constants);
            var inlinedDefs = currentDefs.stream()
                    .map(map -> inlineVariable(map, constants))
                    .collect(Collectors.toList());

            if (inlinedTemplate != currentTemplate || !currentDefs.equals(inlinedDefs)) {
                currentTemplate = inlinedTemplate;
                currentDefs = inlinedDefs;
                changed = true;
            }

            var reachable = computeReachable(currentTemplate, currentDefs);
            var cleaned = eliminateUnusedDefs(currentDefs, reachable);
            changed |= cleaned != currentDefs;
            currentDefs = cleaned;

        } while (changed);

        return new Result(currentDefs, currentTemplate);
    }

}



