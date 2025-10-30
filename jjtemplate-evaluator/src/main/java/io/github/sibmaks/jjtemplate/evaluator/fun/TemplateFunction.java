package io.github.sibmaks.jjtemplate.evaluator.fun;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public interface TemplateFunction<T> {

    T invoke(List<Object> args, Object pipeArg);

    T invoke(List<Object> args);

    String getName();
}
