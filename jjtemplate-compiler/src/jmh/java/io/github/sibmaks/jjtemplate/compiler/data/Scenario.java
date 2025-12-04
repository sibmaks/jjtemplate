package io.github.sibmaks.jjtemplate.compiler.data;

/**
 * Benchmark scenarios
 *
 * @author sibmaks
 * @since 0.4.1
 */
public enum Scenario {
    EMPTY(DataSet.EMPTY, TemplateScript.EMPTY),

    VARS__STRING_CONCAT(DataSet.VARS, TemplateScript.STRING_CONCAT),
    EMPTY__STRING_CONCAT_INLINE(DataSet.EMPTY, TemplateScript.STRING_CONCAT, true),

    VARS__SWITCH(DataSet.VARS, TemplateScript.SWITCH),
    EMPTY__SWITCH_INLINE(DataSet.EMPTY, TemplateScript.SWITCH, true),

    VARS__TERNARY(DataSet.VARS, TemplateScript.TERNARY),
    EMPTY__TERNARY_INLINE(DataSet.EMPTY, TemplateScript.TERNARY, true),

    VARS__SUB_FIELD(DataSet.VARS, TemplateScript.SUB_FIELD),
    EMPTY__SUB_FIELD_INLINE(DataSet.EMPTY, TemplateScript.SUB_FIELD, true),
    ;

    final DataSet dataset;
    final TemplateScript template;
    final boolean inline;

    Scenario(DataSet dataset, TemplateScript template) {
        this(dataset, template, false);
    }

    Scenario(DataSet dataset, TemplateScript template, boolean inline) {
        this.dataset = dataset;
        this.template = template;
        this.inline = inline;
    }

}
