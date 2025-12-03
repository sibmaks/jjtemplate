package io.github.sibmaks.jjtemplate.frontend.listener;

import io.github.sibmaks.jjtemplate.frontend.exception.TemplateParseException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * ANTLR error listener that converts syntax errors into exceptions.
 * <p>
 * Used by the expression parser to ensure that malformed expressions
 * immediately fail with a descriptive error message rather than being
 * silently recovered.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public class ThrowingErrorListener extends BaseErrorListener {

    public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
            throws TemplateParseException {
        throw new TemplateParseException(
                "line " + line + ":" + charPositionInLine + " " + msg
        );
    }
}
