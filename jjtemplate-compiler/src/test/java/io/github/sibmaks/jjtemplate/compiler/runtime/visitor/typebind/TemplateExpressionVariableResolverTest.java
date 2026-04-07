package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.MapTemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateTypeValidationMode;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
class TemplateExpressionVariableResolverTest {

    @Test
    void bindVariableExpressionShouldBindArgumentsEvenWhenRootTypeIsUnknown() {
        var compileContext = new MapTemplateCompileContext(Map.of(), TemplateTypeValidationMode.SOFT);
        var scope = new CompileScope(
                null,
                Map.of("arg", CompileTypeSet.known(FinalValue.class)),
                compileContext
        );
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);

        var argument = new VariableTemplateExpression(
                "arg",
                List.of(new VariableTemplateExpression.GetPropertyChain("name")),
                ".arg.name"
        );
        var expression = new VariableTemplateExpression(
                "unknown",
                List.of(new VariableTemplateExpression.CallMethodChain("call", List.of(argument))),
                ".unknown.call(.arg.name)"
        );

        var bound = assertInstanceOf(
                VariableTemplateExpression.class,
                resolver.bindVariableExpression(expression, scope, bindingEngine)
        );

        assertNotSame(expression, bound);
        var call = assertInstanceOf(
                VariableTemplateExpression.CallMethodChain.class,
                bound.getCallChain().get(0)
        );
        var boundArgument = assertInstanceOf(VariableTemplateExpression.class, call.getArgsExpressions().get(0));
        assertInstanceOf(VariableTemplateExpression.BoundPropertyChain.class, boundArgument.getCallChain().get(0));
    }

    @Test
    void inferVariableTypeShouldResolveNumericStringSegment() {
        var compileContext = new MapTemplateCompileContext(Map.of("value", List.of(String.class)));
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.GetPropertyChain("0")),
                ".value.0"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertTrue(inferred.isKnown());
        assertEquals(List.of(String.class), inferred.types());
    }

    @Test
    void inferVariableTypeShouldReturnUnknownForListIndex() {
        var compileContext = new MapTemplateCompileContext(Map.of("value", List.of(List.class)));
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.GetPropertyChain("0")),
                ".value.0"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertFalse(inferred.isKnown());
    }

    @Test
    void inferVariableTypeShouldReturnUnknownForCustomChain() {
        var compileContext = new MapTemplateCompileContext(Map.of("value", List.of(String.class)));
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        VariableTemplateExpression.Chain customChain = (context, value) -> value;
        var expression = new VariableTemplateExpression("value", List.of(customChain), ".value");

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertFalse(inferred.isKnown());
    }

    @Test
    void inferVariableTypeShouldResolveArrayNumericSegment() {
        var compileContext = new MapTemplateCompileContext(Map.of("value", List.of(String[].class)));
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.GetPropertyChain("0")),
                ".value.0"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertTrue(inferred.isKnown());
        assertEquals(List.of(String.class), inferred.types());
    }

    @Test
    void bindVariableExpressionShouldFailForInvalidArrayPropertyInStrictMode() {
        var compileContext = new MapTemplateCompileContext(
                Map.of("value", List.of(String[].class)),
                TemplateTypeValidationMode.STRICT
        );
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.GetPropertyChain("name")),
                ".value.name"
        );

        var exception = assertThrows(
                TemplateCompilationException.class,
                () -> resolver.bindVariableExpression(expression, scope, bindingEngine)
        );

        assertEquals(
                "Unknown property 'name' in expression '.value.name' for types [[Ljava.lang.String;]",
                exception.getMessage()
        );
    }

    @Test
    void bindVariableExpressionShouldFailForUnknownMethodInStrictMode() {
        var compileContext = new MapTemplateCompileContext(
                Map.of("value", List.of(String.class)),
                TemplateTypeValidationMode.STRICT
        );
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.CallMethodChain("missing", List.of())),
                ".value.missing()"
        );

        var exception = assertThrows(
                TemplateCompilationException.class,
                () -> resolver.bindVariableExpression(expression, scope, bindingEngine)
        );

        assertEquals(
                "Unknown method 'missing' in expression '.value.missing()' for types [java.lang.String]",
                exception.getMessage()
        );
    }

    @Test
    void inferVariableTypeShouldReturnUnknownForAmbiguousMethod() {
        var compileContext = new MapTemplateCompileContext(
                Map.of("value", List.of(StringBuilder.class)),
                TemplateTypeValidationMode.SOFT
        );
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.CallMethodChain(
                        "append",
                        List.of(new ConstantTemplateExpression(null))
                )),
                ".value.append(null)"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertFalse(inferred.isKnown());
    }

    @Test
    void inferVariableTypeShouldReturnUnknownForSoftExtensibleMissingMethod() {
        var compileContext = new MapTemplateCompileContext(
                Map.of("value", List.of(SoftValue.class)),
                TemplateTypeValidationMode.SOFT
        );
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.CallMethodChain("missing", List.of())),
                ".value.missing()"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertFalse(inferred.isKnown());
    }

    @Test
    void inferVariableTypeShouldRespectPreBoundPropertyChain() {
        var compileContext = new MapTemplateCompileContext(Map.of("value", List.of(FinalValue.class)));
        var scope = new CompileScope(null, Map.of(), compileContext);
        var bindingEngine = new TemplateExpressionBindingEngine(compileContext);
        var resolver = new TemplateExpressionVariableResolver(compileContext);
        var resolvedProperty = ReflectionUtils
                .resolveProperty(FinalValue.class, "name")
                .orElseThrow();
        var expression = new VariableTemplateExpression(
                "value",
                List.of(new VariableTemplateExpression.BoundPropertyChain("name", List.of(resolvedProperty))),
                ".value.name"
        );

        var inferred = resolver.inferVariableType(expression, scope, bindingEngine);

        assertTrue(inferred.isKnown());
        assertEquals(List.of(FinalValue.class), inferred.types());
    }

    private static final class FinalValue {
        private final String name = "ok";

        public String getName() {
            return name;
        }
    }

    private static class SoftValue {
    }
}
