package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.RangeTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.SpreadObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ElseTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.ExpressionSwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchCase;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateTypeInferenceVisitor;
import io.github.sibmaks.jjtemplate.frontend.ExpressionParser;
import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParser;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * {@code RootTemplateExpressionFactory} is a high-level factory responsible for
 * compiling raw template objects into executable {@link TemplateExpression} trees.
 * <p>
 * The factory acts as an orchestration layer that combines:
 * <ul>
 *   <li>parsing of template expressions</li>
 *   <li>type inference</li>
 *   <li>construction of expression AST nodes</li>
 * </ul>
 * into a single compilation pipeline.
 * <p>
 * It is intended to be used as the entry point for transforming template
 * definitions and template bodies into expression trees that can later be
 * optimized and evaluated.
 *
 * @author sibmaks
 * @see TemplateExpression
 * @see TemplateExpressionFactory
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class RootTemplateExpressionFactory {
    private final TemplateTypeInferenceVisitor typeInferenceVisitor;
    private final TemplateExpressionFactory expressionFactory;
    private final ExpressionParser expressionParser;

    /**
     * Compiles the given template object into a {@link TemplateExpression}.
     * <p>
     * The input object may represent a constant value, a structured template,
     * or a string containing template expressions. The method parses and
     * transforms it into an executable expression tree.
     *
     * @param object template definition or value to compile
     * @return compiled template expression
     * @throws RuntimeException if the template cannot be parsed or compiled
     */
    public TemplateExpression compile(Object object) {
        if (object == null) {
            return new ConstantTemplateExpression(null);
        }

        if (object instanceof String) {
            var rawExpression = (String) object;
            return compileString(rawExpression);
        }

        if (object instanceof Map<?, ?>) {
            var rawExpression = (Map<String, Object>) object;
            return compileObject(rawExpression);
        }

        if (object instanceof Collection<?>) {
            var rawExpression = (Collection<Object>) object;
            return compileList(rawExpression);
        }

        if (object.getClass().isArray()) {
            return compileArray(object);
        }

        return new ConstantTemplateExpression(object);
    }

    private TemplateExpression compileList(Collection<Object> rawExpression) {
        var items = new ArrayList<ListElement>(rawExpression.size());
        for (var item : rawExpression) {
            compileCollectionItem(items, item);
        }

        return new ListTemplateExpression(items);
    }

    private void compileCollectionItem(ArrayList<ListElement> items, Object item) {
        if (!(item instanceof String)) {
            var compiledItem = compile(item);
            var listElement = new DynamicListElement(compiledItem);
            items.add(listElement);
            return;
        }
        var rawItem = (String) item;
        var templateContext = expressionParser.parse(rawItem);
        var expressionItem = templateContext.accept(expressionFactory);
        var keyType = templateContext.accept(typeInferenceVisitor);
        switch (keyType) {
            case STATIC:
            case EXPRESSION: {
                var listElement = new DynamicListElement(expressionItem);
                items.add(listElement);
                break;
            }
            case CONDITION: {
                var listElement = new ConditionListElement(expressionItem);
                items.add(listElement);
                break;
            }
            case SPREAD: {
                var listElement = new SpreadListElement(expressionItem);
                items.add(listElement);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown key type: " + keyType);
        }
    }

    private TemplateExpression compileArray(Object rawExpression) {
        var length = Array.getLength(rawExpression);
        var items = new ArrayList<ListElement>(length);
        for (int i = 0; i < length; i++) {
            var item = Array.get(rawExpression, i);
            compileCollectionItem(items, item);
        }

        return new ListTemplateExpression(items);
    }

    private TemplateExpression compileString(String rawExpression) {
        var templateContext = expressionParser.parse(rawExpression);
        return templateContext.accept(expressionFactory);
    }

    private TemplateExpression compileObject(Map<String, Object> rawExpression) {
        var elements = new ArrayList<ObjectElement>(rawExpression.size());

        for (var entry : rawExpression.entrySet()) {
            var rawKey = entry.getKey();
            var keyContext = expressionParser.parse(rawKey);
            var keyType = keyContext.accept(typeInferenceVisitor);
            switch (keyType) {
                case STATIC:
                case EXPRESSION:
                case CONDITION: {
                    var expressionKey = keyContext.accept(expressionFactory);
                    var rawValue = entry.getValue();
                    var element = new ObjectFieldElement(
                            expressionKey,
                            compile(rawValue)
                    );
                    elements.add(element);
                    break;
                }
                case SWITCH: {
                    var switchRawValue = (Map<String, Object>) entry.getValue();
                    var element = compileSwitch(keyContext, switchRawValue);
                    elements.add(element);
                    break;
                }
                case SWITCH_ELSE: {
                    var expression = (ElseTemplateExpression) keyContext.accept(expressionFactory);
                    var rawValue = entry.getValue();
                    expression = new ElseTemplateExpression(compile(rawValue));
                    var element = new ObjectFieldElement(
                            expression,
                            expression
                    );
                    elements.add(element);
                    break;
                }
                case RANGE: {
                    var element = compileRange(keyContext, entry.getValue());
                    elements.add(element);
                    break;
                }
                case SPREAD: {
                    var expressionKey = keyContext.accept(expressionFactory);
                    var element = new SpreadObjectElement(expressionKey);
                    elements.add(element);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown key type: " + keyType);
            }
        }

        return new ObjectTemplateExpression(elements);
    }

    private ObjectFieldElement compileSwitch(
            JJTemplateParser.TemplateContext keyContext,
            Map<String, Object> rawValue
    ) {
        var expressionKey = (SwitchTemplateExpression) keyContext.accept(expressionFactory);

        var switchBuilder = SwitchTemplateExpression.builder()
                .switchKey(expressionKey.getSwitchKey())
                .condition(expressionKey.getCondition());

        var compiledCases = (ObjectTemplateExpression) compileObject(rawValue);
        var elements = compiledCases.getElements();
        var switchCases = new ArrayList<SwitchCase>(elements.size());
        for (var element : elements) {
            if (element instanceof ObjectFieldElement) {
                var objectElement = (ObjectFieldElement) element;
                var objectKey = objectElement.getKey();
                if (objectKey instanceof ElseTemplateExpression) {
                    var elseTemplate = (ElseTemplateExpression) objectKey;
                    switchCases.add(elseTemplate);
                } else {
                    var objectValue = objectElement.getValue();
                    var switchCase = new ExpressionSwitchCase(objectKey, objectValue);
                    switchCases.add(switchCase);
                }
            } else {
                throw new IllegalArgumentException("Unknown key type: " + element.getClass());
            }
        }

        var switchExpression = switchBuilder
                .cases(switchCases)
                .build();

        return new ObjectFieldElement(
                expressionKey.getSwitchKey(),
                switchExpression
        );
    }

    private ObjectFieldElement compileRange(
            JJTemplateParser.TemplateContext keyContext,
            Object body
    ) {
        var expressionKey = (RangeTemplateExpression) keyContext.accept(expressionFactory);

        var compiledBody = compile(body);

        var range = RangeTemplateExpression.builder()
                .name(expressionKey.getName())
                .itemVariableName(expressionKey.getItemVariableName())
                .indexVariableName(expressionKey.getIndexVariableName())
                .source(expressionKey.getSource())
                .body(compiledBody)
                .build();

        return new ObjectFieldElement(
                expressionKey.getName(),
                range
        );
    }

}
