package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
@ExtendWith(MockitoExtension.class)
class FormatDateTemplateFunctionTest {
    @InjectMocks
    private FormatDateTemplateFunction function;

    @Test
    void checkFunctionName() {
        var actual = function.getName();
        assertEquals("formatDate", actual);
    }

    @Test
    void formatLocalDateFromArguments() {
        var format = "dd.MM.yyyy";
        var localDate = LocalDate.now();
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateFromPipe() {
        var format = "dd.MM.yyyy";
        var localDate = LocalDate.now();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateTimeFromArguments() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = LocalDateTime.now();
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @Test
    void formatLocalDateTimeFromPipe() {
        var format = "dd.MM.yyyy'T'HH:mm:ss";
        var localDate = LocalDateTime.now();
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = DateTimeFormatter.ofPattern(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("formatDateCases")
    void formatDateFromArguments(String format, Object localDate) {
        var args = List.of(
                ExpressionValue.of(format),
                ExpressionValue.of(localDate)
        );
        var actual = function.invoke(args, ExpressionValue.empty());
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    @ParameterizedTest
    @MethodSource("formatDateCases")
    void formatDateFromPipe(String format, Object localDate) {
        var args = List.of(
                ExpressionValue.of(format)
        );
        var actual = function.invoke(args, ExpressionValue.of(localDate));
        assertFalse(actual.isEmpty());

        var formatter = new SimpleDateFormat(format);
        var excepted = formatter.format(localDate);
        assertEquals(excepted, actual.getValue());
    }

    public static Stream<Arguments> formatDateCases() {
        return Stream.of(
                Arguments.of("dd.MM.yyyy'T'HH:mm:ss", new Date()),
                Arguments.of("dd.MM.yyyy", new Date()),
                Arguments.of("yyyy-MM-dd HH:mm", new Date()),
                Arguments.of("yyyy-MM-dd", new Date())
        );
    }

    @Test
    void onInvalidDateType() {
        var format = "dd.MM.yyyy";
        var args = List.of(
                ExpressionValue.of(format)
        );
        var pipe = ExpressionValue.of(String.class);
        var exception = assertThrows(
                TemplateEvalException.class,
                () -> function.invoke(args, pipe)
        );
        assertEquals("Cannot convert " + String.class + " to TemporalAccessor", exception.getMessage());
    }
}