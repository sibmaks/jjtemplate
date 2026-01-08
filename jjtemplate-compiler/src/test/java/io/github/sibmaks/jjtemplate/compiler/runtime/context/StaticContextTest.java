package io.github.sibmaks.jjtemplate.compiler.runtime.context;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 *
 * @author sibmaks
 */
class StaticContextTest {

    @Test
    void getRootShouldAlwaysReturnNull() {
        var context = Context.empty();

        assertNull(context.getRoot("any"));
        assertNull(context.getRoot(null));
    }

    @Test
    void inShouldThrowUnsupportedOperationException() {
        var context = Context.empty();

        Map<String, Object> layer = mock();
        assertThrows(
                UnsupportedOperationException.class,
                () -> context.in(layer)
        );
    }

    @Test
    void outShouldThrowUnsupportedOperationException() {
        var context = Context.empty();

        assertThrows(
                UnsupportedOperationException.class,
                context::out
        );
    }

    @Test
    void emptyShouldReturnSameInstance() {
        var c1 = Context.empty();
        var c2 = Context.empty();

        assertSame(c1, c2);
    }

}