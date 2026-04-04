package io.github.sibmaks.jjtemplate.compiler.runtime.fun;

import java.util.Locale;

/**
 * Marks template functions that can be configured with a default locale.
 *
 * @param <T> result type
 * @author sibmaks
 * @since 0.9.0
 */
public interface LocaleConfigurableTemplateFunction<T> extends TemplateFunction<T> {

    /**
     * Creates a function instance configured with the provided default locale.
     *
     * @param locale default locale to use when none is passed explicitly
     * @return configured function instance
     */
    TemplateFunction<T> withDefaultLocale(Locale locale);
}
