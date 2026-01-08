package io.github.sibmaks.jjtemplate.compiler.runtime.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Immutable {@link Context} implementation that contains no variable scopes.
 * <p>
 * This context always resolves variables to {@code null} and does not allow
 * modification of scope layers. It is intended for cases where template
 * evaluation does not depend on any external or dynamic variables.
 * </p>
 *
 * <p>
 * Any attempt to push a new scope results in an {@link UnsupportedOperationException}.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class StaticContext implements Context {
    static final Context INSTANCE = new StaticContext();

    @Override
    public Object getRoot(String name) {
        return null;
    }

    @Override
    public void in(Map<String, Object> child) {
        throw new UnsupportedOperationException("StaticContext.in");
    }

    @Override
    public void out() {
        throw new UnsupportedOperationException("StaticContext.out");
    }
}