package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;

/**
 * Base template function for performing string replacement operations.
 * <p>
 * Provides common validation and normalization logic for replace-like
 * string functions while delegating the actual replacement strategy
 * to subclasses.
 * </p>
 *
 * <p>
 * Implementations are expected to define how the replacement is applied
 * (for example: replace first, replace all, regex-based replacement).
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public abstract class AbstractStringReplaceTemplateFunction implements TemplateFunction<String> {

    /**
     * Executes string replacement using subclass-defined strategy.
     * <p>
     * Input arguments are expected to be validated and normalized
     * before invocation.
     * </p>
     *
     * @param source      original string value
     * @param target      value to be replaced
     * @param replacement replacement value
     * @return resulting string after replacement
     */
    protected abstract String replace(String source, String target, String replacement);

    @Override
    public final String invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        if (pipeArg == null) {
            return null;
        }
        var value = String.valueOf(pipeArg);
        var target = String.valueOf(args.get(0));
        var replacement = String.valueOf(args.get(1));
        return replace(value, target, replacement);
    }

    @Override
    public final String invoke(List<Object> args) {
        if (args.size() != 3) {
            throw fail("3 arguments required");
        }
        var arg0 = args.get(0);
        if (arg0 == null) {
            return null;
        }
        var value = String.valueOf(arg0);
        var target = String.valueOf(args.get(1));
        var replacement = String.valueOf(args.get(2));
        return replace(value, target, replacement);
    }

    @Override
    public final String getNamespace() {
        return "string";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
