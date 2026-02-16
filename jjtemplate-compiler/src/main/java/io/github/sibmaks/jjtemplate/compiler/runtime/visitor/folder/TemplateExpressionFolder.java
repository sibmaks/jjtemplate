package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.folder;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListStaticItemElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectStaticFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ConstantSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ElseSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.varusage.VariableUsageCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Performs constant folding and partial evaluation of template expressions.
 * <p>
 * This visitor attempts to reduce expression trees by evaluating any parts
 * that can be computed at compile time. It rewrites expressions into simpler
 * or fully-resolved {@link ConstantTemplateExpression} nodes when possible.
 * </p>
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li>Fold constant arguments of function calls and pipe chains</li>
 *   <li>Invoke non-dynamic functions when all arguments are constants</li>
 *   <li>Merge adjacent constant string fragments in concatenations</li>
 *   <li>Simplify ternary expressions when the condition resolves to a constant</li>
 *   <li>Partially fold variable method-call chains when their arguments are constant</li>
 * </ul>
 *
 * <p>
 * Dynamic functions (those returning {@code true} from {@code isDynamic()})
 * are never evaluated during folding.
 * </p>
 *
 * <p>This visitor is side-effect free and always produces a new expression only
 * when changes are required.</p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateExpressionFolder implements TemplateExpressionVisitor<TemplateExpression> {
    private final ObjectElementFolder objectElementFolder;
    private final ListElementFolder listElementFolder;
    private final SwitchCaseFolder switchCaseFolder;

    /**
     * Creates a folder visitor with helper visitors for object, list, and switch nodes.
     */
    public TemplateExpressionFolder() {
        this.objectElementFolder = new ObjectElementFolder(this);
        this.listElementFolder = new ListElementFolder(this);
        this.switchCaseFolder = new SwitchCaseFolder(this);
    }

    @Override
    public TemplateExpression visit(DynamicFunctionCallTemplateExpression expression) {
        var function = expression.getFunction();
        var argExpression = expression.getArgExpression();
        var foldedArgs = argExpression.visit(this);
        var anyFolded = argExpression != foldedArgs;
        if (!anyFolded) {
            return expression;
        }
        if (!(foldedArgs instanceof ConstantTemplateExpression)) {
            return new DynamicFunctionCallTemplateExpression(function, (ListTemplateExpression) foldedArgs);
        }
        var constantArgs = (ConstantTemplateExpression) foldedArgs;
        var args = constantArgs.getValue();
        if (!(args instanceof List<?>)) {
            throw new IllegalArgumentException("Not a list of function arguments: " + args);
        }
        var argList = (List<Object>) args;
        var constantFunctionExpression = new ConstantFunctionCallTemplateExpression(function, argList);
        if (function.isDynamic()) {
            return constantFunctionExpression;
        }
        var objectFunction = constantFunctionExpression.apply(Context.empty());
        return new ConstantTemplateExpression(objectFunction);
    }

    @Override
    public TemplateExpression visit(ConstantFunctionCallTemplateExpression expression) {
        var function = expression.getFunction();
        if (function.isDynamic()) {
            return expression;
        }
        return new ConstantTemplateExpression(expression.apply(Context.empty()));
    }

    @Override
    public TemplateExpression visit(PipeChainTemplateExpression expression) {
        var root = expression.getRoot();
        var foldedRoot = root.visit(this);
        var anyFolded = root != foldedRoot;

        if (foldedRoot instanceof ConstantTemplateExpression) {
            return tryFoldPipe(expression, (ConstantTemplateExpression) foldedRoot);
        }

        if (anyFolded) {
            return new PipeChainTemplateExpression(
                    foldedRoot,
                    expression.getChain()
            );
        }

        return expression;
    }

    private TemplateExpression tryFoldPipe(
            PipeChainTemplateExpression base,
            ConstantTemplateExpression root
    ) {
        var value = root.getValue();
        var chain = base.getChain();
        for (int i = 0; i < chain.size(); i++) {
            var callExpression = chain.get(i);
            if (callExpression instanceof ConstantFunctionCallTemplateExpression) {
                var constantFunctionCall = (ConstantFunctionCallTemplateExpression) callExpression;
                var function = constantFunctionCall.getFunction();
                if (function.isDynamic()) {
                    return new PipeChainTemplateExpression(
                            new ConstantTemplateExpression(value),
                            chain.subList(i, chain.size())
                    );
                }
                value = constantFunctionCall.apply(Context.empty(), value);
                continue;
            }
            var dynamicFunctionCall = (DynamicFunctionCallTemplateExpression) callExpression;
            var function = dynamicFunctionCall.getFunction();
            var argExpression = dynamicFunctionCall.getArgExpression();
            var foldedArgs = argExpression.visit(this);
            if (!(foldedArgs instanceof ConstantTemplateExpression)) {
                return new PipeChainTemplateExpression(
                        new ConstantTemplateExpression(value),
                        chain.subList(i, chain.size())
                );
            }
            var constantArgs = (ConstantTemplateExpression) foldedArgs;
            var args = constantArgs.getValue();
            if (!(args instanceof List<?>)) {
                throw new IllegalArgumentException("Not a list of function arguments: " + args);
            }
            var argList = (List<Object>) args;
            if (function.isDynamic()) {
                chain.set(i, new ConstantFunctionCallTemplateExpression(function, argList));
                return new PipeChainTemplateExpression(
                        new ConstantTemplateExpression(value),
                        chain.subList(i, chain.size())
                );
            }
            value = function.invoke(argList, value);
        }
        return new ConstantTemplateExpression(value);
    }

    @Override
    public TemplateExpression visit(TemplateConcatTemplateExpression template) {
        var expressions = template.getExpressions();
        var foldedExpressions = new ArrayList<TemplateExpression>(expressions.size());
        var anyFolded = false;

        for (var expression : expressions) {
            var folded = expression.visit(this);
            if (folded != expression) {
                anyFolded = true;
            }
            foldedExpressions.add(folded);
        }

        var merged = new ArrayList<TemplateExpression>();
        StringBuilder buffer = null;

        for (var expr : foldedExpressions) {
            if (expr instanceof ConstantTemplateExpression) {
                var value = ((ConstantTemplateExpression) expr).getValue();
                if (buffer == null) {
                    buffer = new StringBuilder();
                }
                buffer.append(value);
            } else {
                if (buffer != null) {
                    merged.add(new ConstantTemplateExpression(buffer.toString()));
                    buffer = null;
                }
                merged.add(expr);
            }
        }

        if (buffer != null) {
            merged.add(new ConstantTemplateExpression(buffer.toString()));
        }

        if (merged.size() == 1) {
            return merged.get(0);
        }

        if (!anyFolded && merged.size() == expressions.size()) {
            var same = true;
            for (int i = 0; i < expressions.size(); i++) {
                if (!Objects.equals(expressions.get(i), merged.get(i))) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return template;
            }
        }

        return new TemplateConcatTemplateExpression(merged);
    }

    @Override
    public TemplateExpression visit(TernaryTemplateExpression expression) {
        var condition = expression.getCondition();
        var foldedCondition = condition.visit(this);

        if (foldedCondition instanceof ConstantTemplateExpression) {
            var thenTrue = expression.getThenTrue();
            var thenFalse = expression.getThenFalse();
            var constantTernary = new TernaryTemplateExpression(
                    foldedCondition,
                    thenTrue,
                    thenFalse
            );
            var conditionValue = constantTernary.evaluateCondition(Context.empty());
            if (conditionValue) {
                return thenTrue.visit(this);
            }
            return thenFalse.visit(this);
        }

        var thenTrue = expression.getThenTrue();
        var foldedThenTrue = thenTrue.visit(this);

        var thenFalse = expression.getThenFalse();
        var foldedThenFalse = thenFalse.visit(this);

        if (condition != foldedCondition ||
                thenTrue != foldedThenTrue ||
                thenFalse != foldedThenFalse) {
            return new TernaryTemplateExpression(foldedCondition, foldedThenTrue, foldedThenFalse);
        }

        return expression;
    }

    @Override
    public TemplateExpression visit(ConstantTemplateExpression expression) {
        return expression;
    }

    @Override
    public TemplateExpression visit(VariableTemplateExpression expression) {
        var callChain = expression.getCallChain();
        var newCallChain = new ArrayList<VariableTemplateExpression.Chain>();
        var anyFolded = false;
        for (var chain : callChain) {
            if (chain instanceof VariableTemplateExpression.CallMethodChain) {
                var callMethodChain = (VariableTemplateExpression.CallMethodChain) chain;
                var anyArgFolded = false;
                var args = new ArrayList<TemplateExpression>();
                for (var argsExpression : callMethodChain.getArgsExpressions()) {
                    var folded = argsExpression.visit(this);
                    if (folded != argsExpression) {
                        anyArgFolded = true;
                        args.add(folded);
                    } else {
                        args.add(argsExpression);
                    }
                }
                if (anyArgFolded) {
                    anyFolded = true;
                    newCallChain.add(
                            new VariableTemplateExpression.CallMethodChain(
                                    callMethodChain.getMethodName(),
                                    args
                            )
                    );
                } else {
                    newCallChain.add(callMethodChain);
                }
            } else {
                newCallChain.add(chain);
            }
        }

        if (!anyFolded) {
            return expression;
        }

        return new VariableTemplateExpression(
                expression.getRootName(),
                newCallChain
        );
    }

    @Override
    public TemplateExpression visit(ListTemplateExpression expression) {
        var elements = expression.getElements();
        var foldedElements = new ArrayList<ListElement>(elements.size());
        var anyFolded = false;
        for (var element : elements) {
            var folded = element.visit(listElementFolder);
            if (folded.size() != 1) {
                anyFolded = true;
            }
            for (int i = 0; i < folded.size(); i++) {
                var foldedElement = folded.get(i);
                if (i == 0) {
                    anyFolded |= foldedElement != element;
                }
                foldedElements.add(foldedElement);
            }
        }
        var staticItemElements = foldedElements.stream()
                .filter(ListStaticItemElement.class::isInstance)
                .map(ListStaticItemElement.class::cast)
                .collect(Collectors.toList());
        if (staticItemElements.size() == foldedElements.size()) {
            var staticObject = new ListTemplateExpression(foldedElements);
            var evaluated = staticObject.apply(Context.empty());
            return new ConstantTemplateExpression(evaluated);
        }
        if (!anyFolded) {
            return expression;
        }
        return new ListTemplateExpression(foldedElements);
    }

    @Override
    public TemplateExpression visit(RangeTemplateExpression expression) {
        var source = expression.getSource();
        var foldedSource = source.visit(this);
        var anyFolded = source != foldedSource;

        var body = expression.getBody();
        var foldedBody = body.visit(this);
        anyFolded |= body != foldedBody;

        var name = expression.getName();
        var foldedName = name.visit(this);
        anyFolded |= name != foldedName;

        var indexVariableName = expression.getIndexVariableName();
        var itemVariableName = expression.getItemVariableName();
        if (foldedSource instanceof ConstantTemplateExpression) {
            var variableUsageCollector = new VariableUsageCollector();
            foldedBody.visit(variableUsageCollector);
            var variables = variableUsageCollector.getVariables();
            variables.remove(indexVariableName);
            variables.remove(itemVariableName);
            if (variables.isEmpty()) {
                var staticRange = RangeTemplateExpression.builder()
                        .name(foldedName)
                        .indexVariableName(indexVariableName)
                        .itemVariableName(itemVariableName)
                        .source(foldedSource)
                        .body(foldedBody)
                        .build();
                var list = staticRange.apply(Context.of(Map.of()));
                return new ConstantTemplateExpression(list);
            }
        }

        if (anyFolded) {
            return RangeTemplateExpression.builder()
                    .name(foldedName)
                    .indexVariableName(indexVariableName)
                    .itemVariableName(itemVariableName)
                    .source(foldedSource)
                    .body(foldedBody)
                    .build();
        }

        return expression;
    }

    @Override
    public TemplateExpression visit(ObjectTemplateExpression expression) {
        var elements = expression.getElements();
        var foldedElements = new ArrayList<ObjectElement>(elements.size());
        var anyFolded = false;
        for (var element : elements) {
            var folded = element.visit(objectElementFolder);
            if (folded.size() != 1) {
                anyFolded = true;
            }
            for (int i = 0; i < folded.size(); i++) {
                var foldedElement = folded.get(i);
                if (i == 0) {
                    anyFolded |= foldedElement != element;
                }
                foldedElements.add(foldedElement);
            }
        }
        var staticFieldElements = foldedElements.stream()
                .filter(ObjectStaticFieldElement.class::isInstance)
                .map(ObjectStaticFieldElement.class::cast)
                .collect(Collectors.toList());
        if (staticFieldElements.size() == foldedElements.size()) {
            var staticObject = new ObjectTemplateExpression(foldedElements);
            var evaluated = staticObject.apply(Context.empty());
            return new ConstantTemplateExpression(evaluated);
        }
        if (!anyFolded) {
            return expression;
        }
        return new ObjectTemplateExpression(foldedElements);
    }

    @Override
    public TemplateExpression visit(SwitchTemplateExpression expression) {
        var condition = expression.getCondition();
        var foldedCondition = condition.visit(this);
        var anyFolded = condition != foldedCondition;

        var cases = expression.getCases();
        var foldedCases = new ArrayList<SwitchCase>(cases.size());

        var tryFold = foldedCondition instanceof ConstantTemplateExpression;
        Object value = null;
        if (tryFold) {
            var constantCondition = (ConstantTemplateExpression) foldedCondition;
            value = constantCondition.getValue();
        }
        for (var switchCase : cases) {
            var foldedCase = switchCase.visit(switchCaseFolder);
            anyFolded |= switchCase != foldedCase;
            foldedCases.add(foldedCase);
            if (tryFold) {
                if (foldedCase instanceof ConstantSwitchCase) {
                    var constantSwitchCase = (ConstantSwitchCase) foldedCase;
                    var caseValue = constantSwitchCase.getConstant();
                    if (Objects.equals(caseValue, value)) {
                        return constantSwitchCase.getValue();
                    }
                } else if (foldedCase instanceof ElseSwitchCase) {
                    var elseCase = (ElseSwitchCase) foldedCase;
                    return elseCase.getValue();
                } else {
                    tryFold = false;
                }
            }
        }
        if (tryFold) {
            return new ConstantTemplateExpression(null);
        }

        if (anyFolded) {
            return SwitchTemplateExpression.builder()
                    .condition(foldedCondition)
                    .cases(foldedCases)
                    .build();
        }

        return expression;
    }

}
