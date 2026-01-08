package io.github.sibmaks.jjtemplate.compiler.runtime.expression.object;

/**
 * Visitor interface for processing object template elements.
 * <p>
 * Used to traverse or transform {@link ObjectElement} instances without
 * coupling logic to concrete element implementations.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface ObjectElementVisitor<T> {
    /**
     * Visits a dynamic object field element.
     *
     * @param element object field element
     * @return visitor-defined result
     */
    T visit(ObjectFieldElement element);

    /**
     * Visits a spread object element.
     *
     * @param element spread object element
     * @return visitor-defined result
     */
    T visit(SpreadObjectElement element);

    /**
     * Visits a static object field element.
     *
     * @param objectStaticFieldElement static object field element
     * @return visitor-defined result
     */
    T visit(ObjectStaticFieldElement objectStaticFieldElement);
}
