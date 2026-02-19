package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.numberformat;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumberFormatNewTemplateFunctionTest {
    @InjectMocks
    private NumberFormatNewTemplateFunction function;

    @Test
    void checkFunctionNamespace() {
        assertEquals("numberFormat", function.getNamespace());
    }

    @Test
    void checkFunctionName() {
        assertEquals("new", function.getName());
    }

    @Test
    void isStatic() {
        assertFalse(function.isDynamic());
    }

    @Test
    void invokeWithoutArgumentsShouldFail() {
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(List.of()));
        assertEquals("numberFormat:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void pipeInvokeWithoutArgumentsShouldFail() {
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(List.of(), Map.of()));
        assertEquals("numberFormat:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void invokeWithTooManyArgumentsShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of(), Map.of())));
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void pipeInvokeWithTooManyArgumentsShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of()), Map.of()));
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void pipeInvokeWithMoreThanTwoArgumentsShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of(), Map.of()), null));
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void firstArgumentMustBeLocale() {
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(List.of("en-US")));
        assertEquals("numberFormat:new: 1st argument must be Locale, got: java.lang.String", exception.getMessage());
    }

    @Test
    void firstArgumentNullMustFail() {
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(java.util.Collections.singletonList(null)));
        assertEquals("numberFormat:new: 1st argument must be Locale, got null", exception.getMessage());
    }

    @Test
    void secondArgumentMustBeMap() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, "bad-settings")));
        assertEquals("numberFormat:new: 2nd argument must be Map, got: java.lang.String", exception.getMessage());
    }

    @Test
    void secondArgumentNullMustFail() {
        var args = new java.util.ArrayList<Object>();
        args.add(Locale.US);
        args.add(null);
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(args));
        assertEquals("numberFormat:new: 2nd argument must be Map, got null", exception.getMessage());
    }

    @Test
    void pipeSecondArgumentMustBeMap() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US), "bad-settings"));
        assertEquals("numberFormat:new: 2nd argument must be Map, got: java.lang.String", exception.getMessage());
    }

    @Test
    void invokeWithLocaleOnlyUsesDefaultStyle() {
        var result = function.invoke(List.of(Locale.FRANCE));
        assertNotNull(result);

        var expected = NumberFormat.getInstance(Locale.FRANCE).format(1234.5);
        assertEquals(expected, result.format(1234.5));
    }

    @Test
    void invokeWithSettingsStyleNullUsesDefaultStyle() {
        var settings = new HashMap<String, Object>();
        settings.put("style", null);
        settings.put("minimumIntegerDigits", 4);

        var result = function.invoke(List.of(Locale.US, settings));
        assertEquals(4, result.getMinimumIntegerDigits());
    }

    @Test
    void invokeWithNumberStyle() {
        var result = function.invoke(List.of(Locale.US, Map.of("style", "number")));
        assertEquals(NumberFormat.getInstance(Locale.US).format(1234.5), result.format(1234.5));
    }

    @Test
    void invokeWithIntegerStyle() {
        var result = function.invoke(List.of(Locale.US, Map.of("style", "integer")));
        assertEquals(NumberFormat.getIntegerInstance(Locale.US).format(1234.5), result.format(1234.5));
    }

    @Test
    void invokeWithCurrencyStyle() {
        var result = function.invoke(List.of(Locale.US, Map.of("style", "currency")));
        assertEquals(NumberFormat.getCurrencyInstance(Locale.US).format(1234.5), result.format(1234.5));
    }

    @Test
    void invokeWithPercentStyle() {
        var result = function.invoke(List.of(Locale.US, Map.of("style", "percent")));
        assertEquals(NumberFormat.getPercentInstance(Locale.US).format(0.25), result.format(0.25));
    }

    @Test
    void invokeWithUnsupportedStyleShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("style", "scientific"))));
        assertEquals("numberFormat:new: unsupported style: scientific", exception.getMessage());
    }

    @Test
    void applySettingsHappyPath() {
        var settings = Map.<String, Object>of(
                "groupingUsed", false,
                "parseIntegerOnly", true,
                "maximumIntegerDigits", 6,
                "minimumIntegerDigits", 3,
                "maximumFractionDigits", 2,
                "minimumFractionDigits", 2,
                "currency", "USD"
        );

        var result = function.invoke(List.of(Locale.US, settings));

        assertFalse(result.isGroupingUsed());
        assertTrue(result.isParseIntegerOnly());
        assertEquals(6, result.getMaximumIntegerDigits());
        assertEquals(3, result.getMinimumIntegerDigits());
        assertEquals(2, result.getMaximumFractionDigits());
        assertEquals(2, result.getMinimumFractionDigits());
        assertEquals(Currency.getInstance("USD"), result.getCurrency());
    }

    @Test
    void applySettingsCurrencyObject() {
        var result = function.invoke(List.of(Locale.GERMANY, Map.of("currency", Currency.getInstance("EUR"))));
        assertEquals(Currency.getInstance("EUR"), result.getCurrency());
    }

    @Test
    void pipeInvocationWithSettings() {
        var result = function.invoke(List.of(Locale.US), Map.of("minimumFractionDigits", 3));
        assertEquals(3, result.getMinimumFractionDigits());
    }

    @Test
    void pipeInvocationWithSecondArgAndNullPipeUsesSecondArg() {
        var result = function.invoke(List.of(Locale.US, Map.of("maximumFractionDigits", 4)), null);
        assertEquals(4, result.getMaximumFractionDigits());
    }

    @Test
    void pipeInvocationWithNullPipeAndNoSecondArgUsesDefaults() {
        var result = function.invoke(List.of(Locale.US), null);
        assertNotNull(result);
        assertEquals(NumberFormat.getInstance(Locale.US).format(42.5), result.format(42.5));
    }

    @Test
    void roundingModeAsEnum() {
        var result = function.invoke(List.of(Locale.US, Map.of("roundingMode", RoundingMode.DOWN)));
        assertInstanceOf(DecimalFormat.class, result);
        assertEquals(RoundingMode.DOWN, ((DecimalFormat) result).getRoundingMode());
    }

    @Test
    void roundingModeAsString() {
        var result = function.invoke(List.of(Locale.US, Map.of("roundingMode", "UP")));
        assertInstanceOf(DecimalFormat.class, result);
        assertEquals(RoundingMode.UP, ((DecimalFormat) result).getRoundingMode());
    }

    @Test
    void unsupportedSettingShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("unknown", true))));
        assertEquals("numberFormat:new: unsupported setting: unknown", exception.getMessage());
    }

    @Test
    void invalidBooleanSettingShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("groupingUsed", "true"))));
        assertEquals("numberFormat:new: setting 'groupingUsed' must be boolean", exception.getMessage());
    }

    @Test
    void invalidNumericSettingShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("maximumFractionDigits", "2"))));
        assertEquals("numberFormat:new: setting 'maximumFractionDigits' must be numeric", exception.getMessage());
    }

    @Test
    void invalidCurrencySettingShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("currency", 123))));
        assertEquals("numberFormat:new: setting 'currency' must be Currency or string code", exception.getMessage());
    }

    @Test
    void invalidRoundingModeTypeShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("roundingMode", 1))));
        assertEquals("numberFormat:new: setting 'roundingMode' must be RoundingMode or string", exception.getMessage());
    }

    @Test
    void invalidRoundingModeNameShouldFail() {
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(List.of(Locale.US, Map.of("roundingMode", "INVALID"))));
        assertTrue(exception.getMessage().startsWith("numberFormat:new: No enum constant"));
    }

    @Test
    void roundingModeOnNonDecimalFormatShouldFail() throws Exception {
        var method = NumberFormatNewTemplateFunction.class
                .getDeclaredMethod("setRoundingMode", NumberFormat.class, Object.class);
        method.setAccessible(true);

        var cause = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(function, new StubNumberFormat(), RoundingMode.DOWN)).getCause();

        assertInstanceOf(TemplateEvalException.class, cause);
        assertEquals("numberFormat:new: roundingMode is supported only by DecimalFormat", cause.getMessage());
    }

    private static final class StubNumberFormat extends NumberFormat {
        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(number);
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(number);
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            return null;
        }
    }
}
