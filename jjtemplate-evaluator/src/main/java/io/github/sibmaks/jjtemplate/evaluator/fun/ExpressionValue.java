package io.github.sibmaks.jjtemplate.evaluator.fun;

/**
 *
 * @author sibmaks
 */
public class ExpressionValue {
    private static final ExpressionValue EMPTY = new ExpressionValue(true, null);

    private final boolean empty;
    private final Object value;

    private ExpressionValue(boolean empty, Object value) {
        this.empty = empty;
        this.value = value;
    }

    public boolean isEmpty() {
        return empty;
    }

    public Object getValue() {
        return value;
    }

    public static ExpressionValue empty() {
        return EMPTY;
    }

    public static ExpressionValue of(Object value) {
        return new ExpressionValue(false, value);
    }

    @Override
    public String toString() {
        return "ExpressionValue{" +
                "empty=" + empty +
                ", value=" + value +
                '}';
    }
}
