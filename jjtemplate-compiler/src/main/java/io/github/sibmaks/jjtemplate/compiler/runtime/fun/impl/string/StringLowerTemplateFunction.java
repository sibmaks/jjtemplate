package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import java.util.Locale;

/**
 * Template function that converts a string to lowercase.
 *
 * <p>Supports an optional {@link Locale}. If no locale is provided,
 * the default locale is used. Returns {@code null} for {@code null} input.</p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class StringLowerTemplateFunction extends StringCaseTemplateFunction {
    /**
     * Creates a function using the process default locale.
     */
    public StringLowerTemplateFunction() {
        super();
    }

    private StringLowerTemplateFunction(Locale defaultLocale) {
        super(defaultLocale);
    }

    @Override
    protected String toCase(Locale locale, Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.toLowerCase(locale);
    }

    @Override
    public String getName() {
        return "lower";
    }

    @Override
    public StringLowerTemplateFunction withDefaultLocale(Locale locale) {
        return new StringLowerTemplateFunction(locale);
    }
}
