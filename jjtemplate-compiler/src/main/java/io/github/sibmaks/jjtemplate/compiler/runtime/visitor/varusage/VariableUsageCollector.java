package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.varusage;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Collects all variable names referenced inside a template expression tree.
 * <p>
 * The collector walks the entire expression using the visitor pattern and
 * gathers the identifiers of all {@link VariableTemplateExpression} nodes.
 * <p>
 * This information is used by optimization passes (e.g. unused variable
 * elimination) to determine which variables must be preserved.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@Getter
public final class VariableUsageCollector implements
        TemplateExpressionVisitor<Void>,
        ListElementVisitor<Void>,
        ObjectElementVisitor<Void>,
        SwitchCaseVisitor<Void> {
    private final Set<String> variables = new HashSet<>();

    @Override
    public Void visit(ConstantFunctionCallTemplateExpression expression) {
        return null;
    }

    @Override
    public Void visit(DynamicFunctionCallTemplateExpression expression) {
        var argExpression = expression.getArgExpression();
        argExpression.visit(this);
        return null;
    }

    @Override
    public Void visit(PipeChainTemplateExpression expression) {
        var root = expression.getRoot();
        root.visit(this);
        for (var pipeChainFunction : expression.getChain()) {
            pipeChainFunction.visit(this);
        }
        return null;
    }

    @Override
    public Void visit(TemplateConcatTemplateExpression expression) {
        for (var item : expression.getExpressions()) {
            item.visit(this);
        }
        return null;
    }

    @Override
    public Void visit(TernaryTemplateExpression expression) {
        var condition = expression.getCondition();
        condition.visit(this);

        var thenTrue = expression.getThenTrue();
        thenTrue.visit(this);

        var thenFalse = expression.getThenFalse();
        thenFalse.visit(this);

        return null;
    }

    @Override
    public Void visit(ConstantTemplateExpression expression) {
        return null;
    }

    @Override
    public Void visit(VariableTemplateExpression expression) {
        var rootName = expression.getRootName();
        variables.add(rootName);

        var callChain = expression.getCallChain();
        for (var chain : callChain) {
            if (chain instanceof VariableTemplateExpression.CallMethodChain) {
                var callMethodChain = (VariableTemplateExpression.CallMethodChain) chain;
                var argsExpressions = callMethodChain.getArgsExpressions();
                for (var argsExpression : argsExpressions) {
                    argsExpression.visit(this);
                }
            }
        }

        return null;
    }

    @Override
    public Void visit(ListTemplateExpression expression) {
        for (var element : expression.getElements()) {
            element.visit(this);
        }
        return null;
    }

    @Override
    public Void visit(RangeTemplateExpression expression) {
        var name = expression.getName();
        name.visit(this);

        var body = expression.getBody();
        body.visit(this);

        var source = expression.getSource();
        source.visit(this);

        return null;
    }

    @Override
    public Void visit(ElseSwitchCase expression) {
        var value = expression.getValue();
        value.visit(this);
        return null;
    }

    @Override
    public Void visit(ConditionListElement element) {
        var source = element.getSource();
        source.visit(this);
        return null;
    }

    @Override
    public Void visit(DynamicListElement element) {
        var value = element.getValue();
        value.visit(this);
        return null;
    }

    @Override
    public Void visit(SpreadListElement element) {
        var source = element.getSource();
        source.visit(this);
        return null;
    }

    @Override
    public Void visit(ListStaticItemElement element) {
        return null;
    }

    @Override
    public Void visit(ObjectFieldElement element) {
        var key = element.getKey();
        key.visit(this);

        var value = element.getValue();
        value.visit(this);
        return null;
    }

    @Override
    public Void visit(SpreadObjectElement element) {
        var source = element.getSource();
        source.visit(this);
        return null;
    }

    @Override
    public Void visit(ObjectStaticFieldElement objectStaticFieldElement) {
        return null;
    }

    @Override
    public Void visit(ConstantSwitchCase switchCase) {
        var value = switchCase.getValue();
        value.visit(this);
        return null;
    }

    @Override
    public Void visit(ExpressionSwitchCase switchCase) {
        var key = switchCase.getKey();
        key.visit(this);

        var value = switchCase.getValue();
        value.visit(this);
        return null;
    }

    @Override
    public Void visit(ObjectTemplateExpression expression) {
        for (var element : expression.getElements()) {
            element.visit(this);
        }
        return null;
    }

    @Override
    public Void visit(SwitchTemplateExpression expression) {
        var condition = expression.getCondition();
        condition.visit(this);

        for (var switchCase : expression.getCases()) {
            switchCase.visit(this);
        }

        return null;
    }

}
