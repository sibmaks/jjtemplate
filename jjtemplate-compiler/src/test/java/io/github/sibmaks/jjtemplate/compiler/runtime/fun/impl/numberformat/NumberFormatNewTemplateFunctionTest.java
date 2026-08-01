package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.numberformat;

import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

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
        List<Object> args = List.of();
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("numberFormat:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void pipeInvokeWithoutArgumentsShouldFail() {
        List<Object> args = List.of();
        Map<Object, Object> pipeArg = Map.of();
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipeArg)
        );
        assertEquals("numberFormat:new: at least 1 argument required", exception.getMessage());
    }

    @Test
    void invokeWithTooManyArgumentsShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of(), Map.of());
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void pipeInvokeWithTooManyArgumentsShouldFail() {
        Map<Object, Object> object = Map.of();
        List<Object> args = List.of(Locale.US, object);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, object)
        );
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void pipeInvokeWithMoreThanTwoArgumentsShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of(), Map.of());
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, null)
        );
        assertEquals("numberFormat:new: too much arguments passed", exception.getMessage());
    }

    @Test
    void firstArgumentMustBeLocale() {
        List<Object> args = List.of("en-US");
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("numberFormat:new: 1st argument must be Locale, got: java.lang.String", exception.getMessage());
    }

    @Test
    void firstArgumentNullMustFail() {
        List<Object> args = Collections.singletonList(null);
        var exception = assertThrows(TemplateEvalException.class, () -> function.invoke(args));
        assertEquals("numberFormat:new: 1st argument must be Locale, got null", exception.getMessage());
    }

    @Test
    void secondArgumentMustBeMap() {
        List<Object> args = List.of(Locale.US, "bad-settings");
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: 2nd argument must be Map, got: java.lang.String", exception.getMessage());
    }

    @Test
    void secondArgumentNullMustFail() {
        var args = new ArrayList<>();
        args.add(Locale.US);
        args.add(null);
        var exception = assertThrows(TemplateEvalException.class,
                () -> function.invoke(args));
        assertEquals("numberFormat:new: 2nd argument must be Map, got null", exception.getMessage());
    }

    @Test
    void pipeSecondArgumentMustBeMap() {
        List<Object> args = List.of(Locale.US);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, "bad-settings")
        );
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
        List<Object> args = List.of(Locale.US, Map.of("style", "scientific"));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
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
        assertEquals(RoundingMode.DOWN, result.getRoundingMode());
    }

    @Test
    void roundingModeAsString() {
        var result = function.invoke(List.of(Locale.US, Map.of("roundingMode", "UP")));
        assertInstanceOf(DecimalFormat.class, result);
        assertEquals(RoundingMode.UP, result.getRoundingMode());
    }

    @Test
    void unsupportedSettingShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("unknown", true));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: unsupported setting: unknown", exception.getMessage());
    }

    @Test
    void invalidBooleanSettingShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("groupingUsed", "true"));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: setting 'groupingUsed' must be boolean", exception.getMessage());
    }

    @Test
    void invalidNumericSettingShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("maximumFractionDigits", "2"));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: setting 'maximumFractionDigits' must be numeric", exception.getMessage());
    }

    @Test
    void invalidCurrencySettingShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("currency", 123));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: setting 'currency' must be Currency or string code", exception.getMessage());
    }

    @Test
    void invalidRoundingModeTypeShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("roundingMode", 1));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals("numberFormat:new: setting 'roundingMode' must be RoundingMode or string", exception.getMessage());
    }

    @Test
    void invalidRoundingModeNameShouldFail() {
        List<Object> args = List.of(Locale.US, Map.of("roundingMode", "INVALID"));
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args)
        );
        assertEquals(
                "numberFormat:new: No enum constant java.math.RoundingMode.INVALID",
                exception.getMessage()
        );
    }

    @Test
    void roundingModeOnNonDecimalFormatShouldFail() throws Exception {
        var method = NumberFormatNewTemplateFunction.class
                .getDeclaredMethod("setRoundingMode", NumberFormat.class, Object.class);
        method.setAccessible(true);

        var stubNumberFormat = new StubNumberFormat();
        var cause = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(function, stubNumberFormat, RoundingMode.DOWN)
        ).getCause();

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
