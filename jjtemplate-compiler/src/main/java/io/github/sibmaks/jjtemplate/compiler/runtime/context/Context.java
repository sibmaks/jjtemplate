package io.github.sibmaks.jjtemplate.compiler.runtime.context;

import java.util.Map;

/**
 * Represents an evaluation-time variable resolution context.
 * <p>
 * A {@code Context} provides access to variables used during template expression
 * evaluation and supports scoped variable lookup via a stack of variable layers.
 * New scopes may be temporarily pushed during evaluation (for example, during
 * range iteration or conditional execution) and removed after use.
 * </p>
 *
 * <p>
 * Variable lookup is performed from the most recently added scope to the outermost
 * one. If a variable is not found in any active scope, {@code null} is returned.
 * </p>
 *
 * <p>
 * Context implementations are used exclusively at evaluation time and are not
 * intended to be shared between concurrent template renderings.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface Context {

    /**
     * Retrieves a value from the outermost (root) variable scope.
     * <p>
     * The lookup walks through all active variable scopes starting from the
     * most recently added one and returns the first matching entry.
     * If the variable is not found in any scope, {@code null} is returned.
     * </p>
     *
     * @param name the variable name to search for
     * @return the associated value, or {@code null} if not present
     */
    Object getRoot(String name);

    /**
     * Pushes a new variable scope onto the stack.
     * <p>
     * The provided map becomes the highest-priority scope, shadowing variables
     * with the same name from outer contexts.
     * </p>
     *
     * @param child a new scope to add on top of the context stack
     */
    void in(Map<String, Object> child);

    /**
     * Removes the current variable scope from the stack.
     * <p>
     * This operation restores visibility of variables from the next outer scope.
     * If no scopes remain, further calls will cause an exception.
     * </p>
     */
    void out();

    /**
     * Returns a shared immutable empty context.
     * <p>
     * The returned instance contains no variable scopes and should be used
     * when template evaluation does not require an external context.
     * </p>
     *
     * @return a globally shared empty {@link Context} instance
     */
    static Context empty() {
        return StaticContext.INSTANCE;
    }

    /**
     * Creates a new mutable evaluation context backed by the given variable map.
     *
     * @param source initial variable scope
     * @return a new {@link Context} instance
     */
    static Context of(Map<String, Object> source) {
        return new ContextImpl(source);
    }
}