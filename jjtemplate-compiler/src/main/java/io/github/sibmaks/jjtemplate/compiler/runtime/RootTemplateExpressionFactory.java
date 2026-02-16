package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.RangeTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.list.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.SpreadObjectElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.*;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateType;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateTypeInferenceVisitor;
import io.github.sibmaks.jjtemplate.parser.ExpressionParser;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;

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
public final class RootTemplateExpressionFactory {
    private final TemplateTypeInferenceVisitor typeInferenceVisitor;
    private final TemplateExpressionFactory expressionFactory;
    private final ExpressionParser expressionParser;
    private final boolean definitionExpressionFallback;

    /**
     * Creates a factory with explicit definition-expression fallback mode.
     *
     * @param typeInferenceVisitor type inference strategy
     * @param expressionFactory expression compiler for parsed contexts
     * @param expressionParser parser for raw template strings
     * @param definitionExpressionFallback if {@code true}, definition keys can be treated as expressions
     */
    public RootTemplateExpressionFactory(
            TemplateTypeInferenceVisitor typeInferenceVisitor,
            TemplateExpressionFactory expressionFactory,
            ExpressionParser expressionParser,
            boolean definitionExpressionFallback
    ) {
        this.typeInferenceVisitor = typeInferenceVisitor;
        this.expressionFactory = expressionFactory;
        this.expressionParser = expressionParser;
        this.definitionExpressionFallback = definitionExpressionFallback;
    }

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
        return compile(object, false);
    }

    private TemplateExpression compile(Object object, boolean definitionMode) {
        if (object == null) {
            return new ConstantTemplateExpression(null);
        }

        if (object instanceof String) {
            var rawExpression = (String) object;
            return compileString(rawExpression);
        }

        if (object instanceof Map<?, ?>) {
            var rawExpression = (Map<?, ?>) object;
            return compileObject(rawExpression, definitionMode);
        }

        if (object instanceof Collection<?>) {
            var rawExpression = (Collection<?>) object;
            return compileList(rawExpression, definitionMode);
        }

        if (object.getClass().isArray()) {
            return compileArray(object, definitionMode);
        }

        return new ConstantTemplateExpression(object);
    }

    private TemplateExpression compileList(Collection<?> rawExpression, boolean definitionMode) {
        var items = new ArrayList<ListElement>(rawExpression.size());
        for (var item : rawExpression) {
            compileCollectionItem(items, item, definitionMode);
        }

        return new ListTemplateExpression(items);
    }

    private void compileCollectionItem(ArrayList<ListElement> items, Object item, boolean definitionMode) {
        if (!(item instanceof String)) {
            var compiledItem = compile(item, definitionMode);
            var listElement = new DynamicListElement(compiledItem);
            items.add(listElement);
            return;
        }
        var rawItem = (String) item;
        var templateContext = parseCollectionItem(rawItem);
        var expressionItem = expressionFactory.compile(templateContext);
        var keyType = typeInferenceVisitor.infer(templateContext);
        switch (keyType) {
            case CONSTANT:
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

    private JJTemplateParser.TemplateContext parseCollectionItem(String rawItem) {
        try {
            return expressionParser.parse(rawItem);
        } catch (TemplateParseException e) {
            throw new TemplateParseException(String.format("Parse collection item: '%s' failed", rawItem), e);
        }
    }

    private TemplateExpression compileArray(Object rawExpression, boolean definitionMode) {
        var length = Array.getLength(rawExpression);
        var items = new ArrayList<ListElement>(length);
        for (int i = 0; i < length; i++) {
            var item = Array.get(rawExpression, i);
            compileCollectionItem(items, item, definitionMode);
        }

        return new ListTemplateExpression(items);
    }

    private TemplateExpression compileString(String rawExpression) {
        try {
            var templateContext = expressionParser.parse(rawExpression);
            return expressionFactory.compile(templateContext);
        } catch (TemplateParseException e) {
            throw new TemplateParseException(String.format("Parse string: '%s' failed", rawExpression), e);
        }
    }

    /**
     * Compiles the given template object into a {@link ObjectTemplateExpression}.
     * <p>
     * The input object represent a structured template. The method parses and
     * transforms it into an executable expression tree.
     *
     * @param rawExpression value map to compile
     * @return compiled template expression
     */
    public ObjectTemplateExpression compileObject(Map<?, ?> rawExpression) {
        return compileObject(rawExpression, false);
    }

    /**
     * Compiles a definitions map using definition-expression fallback settings.
     *
     * @param rawExpression definitions source map
     * @return compiled object template expression
     */
    public ObjectTemplateExpression compileDefinitionObject(Map<?, ?> rawExpression) {
        return compileObject(rawExpression, definitionExpressionFallback);
    }

    private ObjectTemplateExpression compileObject(
            Map<?, ?> rawExpression,
            boolean definitionMode
    ) {
        var elements = new ArrayList<ObjectElement>(rawExpression.size());

        for (var entry : rawExpression.entrySet()) {
            var rawKey = entry.getKey();
            var keyContext = parseObjectKey(String.valueOf(rawKey), definitionMode, false);
            var keyType = typeInferenceVisitor.infer(keyContext);
            switch (keyType) {
                case CONSTANT:
                case EXPRESSION: {
                    var expressionKey = expressionFactory.compile(keyContext);
                    var rawValue = entry.getValue();
                    var element = new ObjectFieldElement(
                            expressionKey,
                            compile(rawValue, definitionMode)
                    );
                    elements.add(element);
                    break;
                }
                case SWITCH: {
                    var switchRawValue = (Map<?, ?>) entry.getValue();
                    var element = compileSwitch(keyContext, switchRawValue, definitionMode);
                    elements.add(element);
                    break;
                }
                case RANGE: {
                    var element = compileRange(keyContext, entry.getValue(), definitionMode);
                    elements.add(element);
                    break;
                }
                case CONDITION:
                case SPREAD: {
                    var expressionKey = expressionFactory.compile(keyContext);
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

    private JJTemplateParser.TemplateContext parseObjectKey(
            String rawKey,
            boolean definitionMode,
            boolean forceExpression
    ) {
        var expressionCandidate = rawKey;
        var hasBrackets = containsExpression(rawKey);
        if (!hasBrackets && (forceExpression || (definitionMode && looksLikeDefinitionExpression(rawKey)))) {
            expressionCandidate = "{{ " + rawKey + " }}";
        }
        try {
            return expressionParser.parse(expressionCandidate);
        } catch (TemplateParseException e) {
            throw new TemplateParseException(String.format("Parse object field: '%s' failed", rawKey), e);
        }
    }

    private boolean containsExpression(String rawKey) {
        return rawKey.contains("{{");
    }

    private boolean looksLikeDefinitionExpression(String rawKey) {
        var lower = rawKey.trim().toLowerCase();
        return lower.contains(" switch ") || lower.contains(" range ");
    }

    private ObjectFieldElement compileSwitch(
            JJTemplateParser.TemplateContext keyContext,
            Map<?, ?> rawValue,
            boolean definitionMode
    ) {
        var switchCases = new ArrayList<SwitchCase>(rawValue.size());
        var elseSwitchCases = new ArrayList<SwitchCase>(1);

        for (var entry : rawValue.entrySet()) {
            var caseKeyRaw = entry.getKey();
            var caseKeyContext = parseObjectKey(String.valueOf(caseKeyRaw), false, definitionMode);
            var caseKeyType = typeInferenceVisitor.infer(caseKeyContext);
            var caseValueRaw = entry.getValue();
            if (caseKeyType == TemplateType.SWITCH) {
                if (!(caseValueRaw instanceof Map<?, ?>)) {
                    throw new IllegalArgumentException(String.format("Expected a map entry for '%s'", caseKeyRaw));
                }
                var objectValue = compileSwitch(caseKeyContext, (Map<?, ?>) caseValueRaw, definitionMode);
                var switchCase = new ExpressionSwitchCase(objectValue.getKey(), objectValue.getValue());
                switchCases.add(switchCase);
            } else if (caseKeyType == TemplateType.SWITCH_ELSE) {
                var objectValue = compile(caseValueRaw, definitionMode);
                var switchCase = new ElseSwitchCase(objectValue);
                elseSwitchCases.add(switchCase);
            } else {
                var objectKey = expressionFactory.compile(caseKeyContext);
                var objectValue = compile(caseValueRaw, definitionMode);
                var switchCase = new ExpressionSwitchCase(objectKey, objectValue);
                switchCases.add(switchCase);
            }
        }
        switchCases.addAll(elseSwitchCases);

        var expressionKey = (SwitchDefinitionTemplateExpression) expressionFactory.compile(keyContext);
        var switchKey = expressionKey.getKey();
        var condition = expressionKey.getCondition();
        var switchExpression = SwitchTemplateExpression.builder()
                .condition(condition)
                .cases(switchCases)
                .build();

        return new ObjectFieldElement(
                switchKey,
                switchExpression
        );
    }

    private ObjectFieldElement compileRange(
            JJTemplateParser.TemplateContext keyContext,
            Object body,
            boolean definitionMode
    ) {
        var expressionKey = (RangeTemplateExpression) expressionFactory.compile(keyContext);

        var compiledBody = compile(body, definitionMode);

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
