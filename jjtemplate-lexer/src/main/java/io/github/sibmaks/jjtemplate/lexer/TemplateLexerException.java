package io.github.sibmaks.jjtemplate.lexer;

/**
 * @author sibmaks
 */
public class TemplateLexerException extends RuntimeException {
    private final int position;

    public TemplateLexerException(String message, int position) {
        super(message + " at position " + position);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}