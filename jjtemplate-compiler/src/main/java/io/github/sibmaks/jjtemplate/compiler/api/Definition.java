package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.LinkedHashMap;

/**
 * Represents a definition context used during template compilation or evaluation.
 * <p>
 * Extends {@link LinkedHashMap} to store named values such as variables, functions,
 * or configuration data accessible within a template.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public final class Definition extends LinkedHashMap<String, Object> {
}
