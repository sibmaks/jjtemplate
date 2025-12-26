package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link CompiledTemplate} that executes a compiled template node.
 * <p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@ToString
@AllArgsConstructor
public final class CompiledTemplateImpl implements CompiledTemplate {

    /**
     * The list of compiled internal variables.
     */
    private final List<ObjectFieldElement> internalVariables;

    /**
     * The root abstract syntax tree node representing the compiled template.
     */
    private final TemplateExpression compiledTemplate;

    @Override
    public Object render(Map<String, Object> context) {
        var local = new HashMap<>(context);
        var localContext = Context.of(local);
        evalDefinitions(local, localContext);
        return compiledTemplate.apply(localContext);
    }

    private void evalDefinitions(
            Map<String, Object> context,
            Context localContext
    ) {
        for (var variable : internalVariables) {
            var key = variable.getKey();
            var staticNameValue = key.apply(localContext);
            var value = variable.getValue();
            var selectedValue = value.apply(localContext);
            context.put(String.valueOf(staticNameValue), selectedValue);
        }
    }

}