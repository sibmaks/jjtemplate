package io.github.sibmaks.jjtemplate.parser;


/**
 *
 * @author sibmaks
 */
public class TemplateParserException extends RuntimeException {
    public TemplateParserException(String message, int pos) {
        super(message + " at token position " + pos);
    }
}