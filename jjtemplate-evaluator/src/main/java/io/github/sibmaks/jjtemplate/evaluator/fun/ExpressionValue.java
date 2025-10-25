package io.github.sibmaks.jjtemplate.evaluator.fun;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author sibmaks
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpressionValue {
    private static final ExpressionValue EMPTY = new ExpressionValue(true, null);

    private final boolean empty;
    private final Object value;

    public static ExpressionValue empty() {
        return EMPTY;
    }

    public static ExpressionValue of(Object value) {
        return new ExpressionValue(false, value);
    }

}
