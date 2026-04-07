package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.MapTemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateTypeValidationMode;
import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.ConstantFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.function.DynamicFunctionCallTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ConditionListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.DynamicListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.ListTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.SpreadListElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.SpreadObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author sibmaks
 */
class TemplateExpressionBindingEngineTest {

    @Test
    void bindShouldExposeInternalVariableTypeToTemplateScope() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var template = new VariableTemplateExpression(
                "user",
                List.of(new VariableTemplateExpression.GetPropertyChain("name")),
                ".user.name"
        );
        var compiled = new CompiledTemplateImpl(
                List.of(new ObjectFieldElement(
                        new ConstantTemplateExpression("user"),
                        new ConstantTemplateExpression(new FinalValue("Alice"))
                )),
                template
        );

        var bound = engine.bind(compiled);

        var variable = assertInstanceOf(VariableTemplateExpression.class, bound.getCompiledTemplate());
        assertInstanceOf(VariableTemplateExpression.BoundPropertyChain.class, variable.getCallChain().get(0));
    }

    @Test
    void bindExpressionShouldKeepConstantFunctionAndUnknownExpressionAsIs() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var scope = new CompileScope(null, Map.of(), context);
        TemplateFunction<Object> function = mock();
        var constantFunction = new ConstantFunctionCallTemplateExpression(function, List.of(), "f()");
        TemplateExpression custom = new TemplateExpression() {
            @Override
            public Object apply(Context context) {
                return null;
            }

            @Override
            public <T> T visit(TemplateExpressionVisitor<T> visitor) {
                return null;
            }
        };

        assertSame(constantFunction, engine.bindExpression(constantFunction, scope));
        assertSame(custom, engine.bindExpression(custom, scope));
    }

    @Test
    void inferExpressionTypeShouldMergeTernaryBranches() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var scope = new CompileScope(null, Map.of(), context);
        var ternary = new TernaryTemplateExpression(
                new ConstantTemplateExpression(true),
                new ConstantTemplateExpression("text"),
                new ConstantTemplateExpression(1),
                "cond"
        );

        var inferred = engine.inferExpressionType(ternary, scope);

        assertTrue(inferred.isKnown());
        assertTrue(inferred.types().contains(String.class));
        assertTrue(inferred.types().contains(Integer.class));
    }

    @Test
    void inferRangeItemTypeShouldReturnUnknownForNonArray() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);

        var inferred = engine.inferRangeItemType(CompileTypeSet.known(List.class));

        assertFalse(inferred.isKnown());
    }

    @Test
    void bindExpressionShouldTraverseCompositeNodes() {
        var context = new MapTemplateCompileContext(
                Map.of(
                        "user", List.of(FinalValue.class),
                        "items", List.of(FinalValue[].class)
                ),
                TemplateTypeValidationMode.SOFT
        );
        var engine = new TemplateExpressionBindingEngine(context);
        var scope = new CompileScope(null, Map.of(), context);

        var variable = new VariableTemplateExpression("user", List.of(new VariableTemplateExpression.GetPropertyChain("name")), ".user.name");
        var list = new ListTemplateExpression(List.of(
                new DynamicListElement(variable),
                new ConditionListElement(variable),
                new SpreadListElement(variable)
        ));
        var object = new ObjectTemplateExpression(List.of(
                new ObjectFieldElement(variable, variable),
                new SpreadObjectElement(variable)
        ));
        var range = RangeTemplateExpression.builder()
                .name(new ConstantTemplateExpression("items"))
                .itemVariableName("item")
                .indexVariableName("index")
                .source(new VariableTemplateExpression("items", List.of(), ".items"))
                .body(new VariableTemplateExpression("item", List.of(new VariableTemplateExpression.GetPropertyChain("name")), ".item.name"))
                .sourceExpression("range")
                .build();
        var switchExpression = SwitchTemplateExpression.builder()
                .condition(variable)
                .cases(List.of(
                        new ConstantSwitchCase("a", variable),
                        ElseSwitchCase.builder().value(variable).build(),
                        new ExpressionSwitchCase(variable, variable)
                ))
                .sourceExpression("switch")
                .build();
        TemplateFunction<Object> function = mock();
        var dynamicFunction = new DynamicFunctionCallTemplateExpression(function, new ListTemplateExpression(List.of(new DynamicListElement(variable))), "f(.user.name)");
        var pipe = new PipeChainTemplateExpression(variable, List.of(dynamicFunction), "pipe");
        var concat = new TemplateConcatTemplateExpression(List.of(variable, range, object, switchExpression, pipe, list));

        var bound = engine.bindExpression(concat, scope);

        assertInstanceOf(TemplateConcatTemplateExpression.class, bound);
    }

    @Test
    void bindShouldSkipScopeRegistrationForNonStringAndNullDefinitions() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var compiled = new CompiledTemplateImpl(
                List.of(
                        new ObjectFieldElement(new ConstantTemplateExpression(1), new ConstantTemplateExpression("value")),
                        new ObjectFieldElement(new ConstantTemplateExpression("user"), null)
                ),
                new ConstantTemplateExpression("ok")
        );

        var bound = engine.bind(compiled);

        assertEquals("ok", bound.getCompiledTemplate().apply(Context.empty()));
    }

    @Test
    void bindExpressionShouldKeepUnchangedDynamicFunctionRangeAndSwitchNodes() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var scope = new CompileScope(null, Map.of(), context);
        TemplateFunction<Object> function = mock();

        var dynamic = new DynamicFunctionCallTemplateExpression(function, new ListTemplateExpression(List.of()), "f()");
        assertSame(dynamic, engine.bindExpression(dynamic, scope));

        var range = RangeTemplateExpression.builder()
                .name(new ConstantTemplateExpression("items"))
                .itemVariableName("item")
                .indexVariableName("index")
                .source(new ConstantTemplateExpression(new String[]{"a"}))
                .body(new ConstantTemplateExpression("body"))
                .sourceExpression("range")
                .build();
        assertSame(range, engine.bindExpression(range, scope));

        var switchExpression = SwitchTemplateExpression.builder()
                .condition(new ConstantTemplateExpression(true))
                .cases(List.of(
                        new ConstantSwitchCase("a", new ConstantTemplateExpression("x")),
                        ElseSwitchCase.builder().value(new ConstantTemplateExpression("y")).build(),
                        new ExpressionSwitchCase(new ConstantTemplateExpression("k"), new ConstantTemplateExpression("v")),
                        new SwitchCase() {
                            @Override
                            public boolean matches(Object condition, Context context) {
                                return false;
                            }

                            @Override
                            public Object evaluate(Context context, Object condition) {
                                return null;
                            }

                            @Override
                            public <T> T visit(SwitchCaseVisitor<T> visitor) {
                                return null;
                            }
                        }
                ))
                .sourceExpression("switch")
                .build();
        assertSame(switchExpression, engine.bindExpression(switchExpression, scope));
    }

    @Test
    void inferExpressionTypeShouldHandleUnknownAndCollectionKinds() {
        var context = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var engine = new TemplateExpressionBindingEngine(context);
        var scope = new CompileScope(null, Map.of(), context);
        TemplateExpression custom = new TemplateExpression() {
            @Override
            public Object apply(Context context) {
                return null;
            }

            @Override
            public <T> T visit(TemplateExpressionVisitor<T> visitor) {
                return null;
            }
        };
        var ternary = new TernaryTemplateExpression(
                new ConstantTemplateExpression(true),
                new ConstantTemplateExpression("text"),
                new ConstantTemplateExpression(null),
                "cond"
        );

        assertFalse(engine.inferExpressionType(custom, scope).isKnown());
        assertEquals(List.of(List.class), engine.inferExpressionType(new ListTemplateExpression(List.of()), scope).types());
        assertEquals(List.of(Map.class), engine.inferExpressionType(new ObjectTemplateExpression(List.of()), scope).types());
        assertFalse(engine.inferExpressionType(ternary, scope).isKnown());
    }

    private static final class FinalValue {
        private final String name;

        private FinalValue(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
