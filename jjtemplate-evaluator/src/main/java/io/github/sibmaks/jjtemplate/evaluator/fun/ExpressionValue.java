package io.github.sibmaks.jjtemplate.evaluator.fun;

import lombok.*;

/**
 *
 * @author sibmaks
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpressionValue {
    private static final ExpressionValue EMPTY_INSTANCE = new ExpressionValue(true, null);

    private final boolean empty;
    private final Object value;

    public static ExpressionValue empty() {
        return EMPTY_INSTANCE;
    }

    public static ExpressionValue of(Object value) {
        return new ExpressionValue(false, value);
    }

}
