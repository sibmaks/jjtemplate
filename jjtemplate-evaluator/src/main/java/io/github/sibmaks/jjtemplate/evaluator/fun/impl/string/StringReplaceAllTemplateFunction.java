package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

/**
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringReplaceAllTemplateFunction extends AbstractStringReplaceTemplateFunction {

    @Override
    protected String replace(String value, String target, String replacement) {
        var string = String.valueOf(value);
        return string.replaceAll(target, replacement);
    }

    @Override
    public String getName() {
        return "replaceAll";
    }
}
