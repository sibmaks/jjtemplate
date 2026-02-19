package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.numberformat;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Template function that creates a new {@link NumberFormat} instance.
 *
 * <p>Supports locale and settings map in direct or pipe forms.</p>
 *
 * @author sibmaks
 * @since 0.6.0
 */
public final class NumberFormatNewTemplateFunction implements TemplateFunction<NumberFormat> {

    @Override
    public NumberFormat invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        if (args.size() > 2 || args.size() == 2 && pipeArg != null) {
            throw fail("too much arguments passed");
        }
        var locale = asLocale(args.get(0));
        Map<?, ?> settings = null;
        if (args.size() == 2) {
            settings = asSettings(args.get(1));
        } else if (pipeArg != null) {
            settings = asSettings(pipeArg);
        }
        var format = createFormat(locale, settings);
        applySettings(format, settings);
        return format;
    }

    @Override
    public NumberFormat invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        if (args.size() > 2) {
            throw fail("too much arguments passed");
        }
        var locale = asLocale(args.get(0));
        Map<?, ?> settings = null;
        if (args.size() == 2) {
            settings = asSettings(args.get(1));
        }
        var format = createFormat(locale, settings);
        applySettings(format, settings);
        return format;
    }

    private Locale asLocale(Object value) {
        if (value instanceof Locale) {
            return (Locale) value;
        }
        if (value == null) {
            throw fail("1st argument must be Locale, got null");
        }
        throw fail("1st argument must be Locale, got: " + value.getClass().getName());
    }

    private Map<?, ?> asSettings(Object value) {
        if (value instanceof Map<?, ?>) {
            return (Map<?, ?>) value;
        }
        if (value == null) {
            throw fail("2nd argument must be Map, got null");
        }
        throw fail("2nd argument must be Map, got: " + value.getClass().getName());
    }

    private NumberFormat createFormat(Locale locale, Map<?, ?> settings) {
        if (settings == null) {
            return NumberFormat.getInstance(locale);
        }
        var style = settings.get("style");
        if (style == null) {
            return NumberFormat.getInstance(locale);
        }
        var styleString = style.toString().toLowerCase(Locale.ROOT);
        switch (styleString) {
            case "number":
                return NumberFormat.getInstance(locale);
            case "integer":
                return NumberFormat.getIntegerInstance(locale);
            case "currency":
                return NumberFormat.getCurrencyInstance(locale);
            case "percent":
                return NumberFormat.getPercentInstance(locale);
            default:
                throw fail("unsupported style: " + style);
        }
    }

    private void applySettings(NumberFormat format, Map<?, ?> settings) {
        if (settings == null) {
            return;
        }
        try {
            for (var entry : settings.entrySet()) {
                var key = entry.getKey().toString();
                var value = entry.getValue();
                switch (key) {
                    case "style":
                        break;
                    case "groupingUsed":
                        format.setGroupingUsed(asBoolean(key, value));
                        break;
                    case "parseIntegerOnly":
                        format.setParseIntegerOnly(asBoolean(key, value));
                        break;
                    case "maximumIntegerDigits":
                        format.setMaximumIntegerDigits(asInt(key, value));
                        break;
                    case "minimumIntegerDigits":
                        format.setMinimumIntegerDigits(asInt(key, value));
                        break;
                    case "maximumFractionDigits":
                        format.setMaximumFractionDigits(asInt(key, value));
                        break;
                    case "minimumFractionDigits":
                        format.setMinimumFractionDigits(asInt(key, value));
                        break;
                    case "currency":
                        format.setCurrency(asCurrency(value));
                        break;
                    case "roundingMode":
                        setRoundingMode(format, value);
                        break;
                    default:
                        throw fail("unsupported setting: " + key);
                }
            }
        } catch (IllegalArgumentException e) {
            throw fail(e.getMessage(), e);
        }
    }

    private static boolean asBoolean(String key, Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("setting '" + key + "' must be boolean");
    }

    private static int asInt(String key, Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("setting '" + key + "' must be numeric");
    }

    private static Currency asCurrency(Object value) {
        if (value instanceof Currency) {
            return (Currency) value;
        }
        if (value instanceof String) {
            var currencyCode = (String) value;
            return Currency.getInstance(currencyCode);
        }
        throw new IllegalArgumentException("setting 'currency' must be Currency or string code");
    }

    private void setRoundingMode(NumberFormat format, Object value) {
        if (!(format instanceof DecimalFormat)) {
            throw fail("roundingMode is supported only by DecimalFormat");
        }
        var decimalFormat = (DecimalFormat) format;
        if (value instanceof RoundingMode) {
            decimalFormat.setRoundingMode((RoundingMode) value);
            return;
        }
        if (value instanceof String) {
            var valueString = (String) value;
            decimalFormat.setRoundingMode(RoundingMode.valueOf(valueString));
            return;
        }
        throw new IllegalArgumentException("setting 'roundingMode' must be RoundingMode or string");
    }

    @Override
    public String getNamespace() {
        return "numberFormat";
    }

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
