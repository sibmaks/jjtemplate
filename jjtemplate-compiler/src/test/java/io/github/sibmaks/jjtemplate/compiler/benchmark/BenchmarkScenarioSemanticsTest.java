package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.impl.StaticCompiledTemplateImpl;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.VariableTemplateExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkScenarioSemanticsTest {

    @ParameterizedTest
    @EnumSource(BenchmarkScenario.class)
    void optimizedAndUnoptimizedScenariosShouldBeEquivalent(BenchmarkScenario scenario) {
        var benchmarkCase = BenchmarkFixtures.create(scenario);

        var unoptimized = compiler(false).compile(benchmarkCase.getScript());
        var optimized = compiler(true).compile(benchmarkCase.getScript());

        assertEquals(benchmarkCase.getExpected(), unoptimized.render(benchmarkCase.getContext()));
        assertEquals(benchmarkCase.getExpected(), optimized.render(benchmarkCase.getContext()));
    }

    @Test
    void collectionScalingFixtureShouldPreserveRequestedCardinality() {
        for (var dataLocation : CollectionDataLocation.values()) {
            for (var size : new int[]{1, 10, 100, 1000}) {
                var benchmarkCase = BenchmarkFixtures.collection(size, dataLocation);
                var rendered = assertInstanceOf(
                        java.util.List.class,
                        compiler(true).compile(benchmarkCase.getScript())
                                .render(benchmarkCase.getContext())
                );
                assertEquals(size, rendered.size());
                assertEquals(benchmarkCase.getExpected(), rendered);
            }
        }
    }

    @Test
    void onlyStaticScenarioShouldBecomeStaticCompiledTemplate() {
        var staticCase = BenchmarkFixtures.create(BenchmarkScenario.STATIC_LITERAL);
        assertInstanceOf(
                StaticCompiledTemplateImpl.class,
                compiler(true).compile(staticCase.getScript())
        );

        for (var scenario : BenchmarkScenario.values()) {
            if (scenario == BenchmarkScenario.STATIC_LITERAL) {
                continue;
            }
            var benchmarkCase = BenchmarkFixtures.create(scenario);
            assertInstanceOf(
                    CompiledTemplateImpl.class,
                    compiler(true).compile(benchmarkCase.getScript()),
                    scenario.name()
            );
        }
    }

    @ParameterizedTest
    @EnumSource(TypeBindingScenario.class)
    void dynamicAndExplicitContextScenariosShouldBeEquivalent(TypeBindingScenario scenario) {
        var benchmarkCase = TypeBindingFixtures.create(scenario);
        var compiler = TemplateCompiler.getInstance();

        var dynamic = benchmarkCase.compile(compiler, TypeBindingMode.DYNAMIC);
        var explicitContext = benchmarkCase.compile(compiler, TypeBindingMode.EXPLICIT_CONTEXT);

        assertEquals(benchmarkCase.getExpected(), dynamic.render(benchmarkCase.getContext()));
        assertEquals(benchmarkCase.getExpected(), explicitContext.render(benchmarkCase.getContext()));
        assertDynamicFirstChain(dynamic);

        var explicitVariable = variableExpression(explicitContext);
        if (benchmarkCase.isBoundPathExpected()) {
            assertTrue(
                    explicitVariable.getCallChain().get(0)
                            instanceof VariableTemplateExpression.BoundPropertyChain
                            || explicitVariable.getCallChain().get(0)
                            instanceof VariableTemplateExpression.BoundMethodChain
            );
        } else {
            assertInstanceOf(VariableTemplateExpression.GetPropertyChain.class, explicitVariable.getCallChain().get(0));
        }
    }

    private static TemplateCompiler compiler(boolean optimize) {
        return TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
    }

    private static void assertDynamicFirstChain(CompiledTemplate compiledTemplate) {
        var variable = variableExpression(compiledTemplate);
        assertTrue(
                variable.getCallChain().get(0)
                        instanceof VariableTemplateExpression.GetPropertyChain
                        || variable.getCallChain().get(0)
                        instanceof VariableTemplateExpression.CallMethodChain
        );
    }

    private static VariableTemplateExpression variableExpression(CompiledTemplate compiledTemplate) {
        var compiled = assertInstanceOf(CompiledTemplateImpl.class, compiledTemplate);
        return assertInstanceOf(
                VariableTemplateExpression.class,
                compiled.getCompiledTemplate()
        );
    }
}
