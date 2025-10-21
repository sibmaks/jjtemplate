package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class TemplateScript {
    private List<Definition> definitions;
    private Object template;

    public TemplateScript() {
    }

    public TemplateScript(List<Definition> definitions, Object template) {
        this.definitions = definitions;
        this.template = template;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public Object getTemplate() {
        return template;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }
}
