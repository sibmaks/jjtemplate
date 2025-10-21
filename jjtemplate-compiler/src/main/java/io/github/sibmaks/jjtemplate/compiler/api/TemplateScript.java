package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class TemplateScript {
    private final List<Definition> definitions;
    private final Object template;

    public TemplateScript(List<Definition> definitions, Object template) {
        this.definitions = definitions;
        this.template = template;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public Object getTemplate() {
        return template;
    }
}
