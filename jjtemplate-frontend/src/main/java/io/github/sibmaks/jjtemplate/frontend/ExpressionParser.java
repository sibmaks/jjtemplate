package io.github.sibmaks.jjtemplate.frontend;

import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateLexer;
import io.github.sibmaks.jjtemplate.frontend.antlr.JJTemplateParser;
import io.github.sibmaks.jjtemplate.frontend.listener.ThrowingErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;

/**
 * Parses template expression strings into ANTLR parse trees.
 * <p>
 * This parser configures an ANTLR lexer and parser using the JJTemplate grammar
 * and produces a {@link JJTemplateParser.TemplateContext} representing the
 * parsed expression. Error listeners are configured to throw exceptions on
 * syntax errors.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public class ExpressionParser {
    /**
     * Parses the given expression text into a template parse tree.
     *
     * @param input expression string to parse
     * @return the root ANTLR parse context representing the expression
     * @throws RuntimeException if the input contains syntax errors
     */
    public JJTemplateParser.TemplateContext parse(String input) {
        var lexer = new JJTemplateLexer(CharStreams.fromString(input));
        var tokens = new CommonTokenStream(lexer);
        tokens.fill();

        var parser = new JJTemplateParser(tokens);

        parser.removeErrorListeners();
        // parser.addErrorListener(new DiagnosticErrorListener());
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        return parser.template();
    }
}
