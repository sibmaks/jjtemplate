package io.github.sibmaks.jjtemplate.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor
public class Pair<K, V> {
    private final K first;
    private final V second;
}
