package io.github.sibmaks.jjtemplate.compiler.runtime.expression.list;

/**
 *
 * Visitor interface for processing list template elements.
 * <p>
 * Used to traverse or transform {@link ListElement} instances without
 * coupling logic to concrete element implementations.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public interface ListElementVisitor<T> {
    /**
     * Visits a conditional list element.
     *
     * @param element conditional list element
     * @return visitor-defined result
     */
    T visit(ConditionListElement element);

    /**
     * Visits a dynamic list element.
     *
     * @param element dynamic list element
     * @return visitor-defined result
     */
    T visit(DynamicListElement element);

    /**
     * Visits a spread list element.
     *
     * @param element spread list element
     * @return visitor-defined result
     */
    T visit(SpreadListElement element);

    /**
     * Visits a static list item element.
     *
     * @param element static list item element
     * @return visitor-defined result
     */
    T visit(ListStaticItemElement element);
}
