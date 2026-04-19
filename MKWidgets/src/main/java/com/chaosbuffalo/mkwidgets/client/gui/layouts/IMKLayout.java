package com.chaosbuffalo.mkwidgets.client.gui.layouts;


import com.chaosbuffalo.mkwidgets.client.gui.constraints.IConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Widget contract for containers that perform a layout pass over their children.
 * <p>
 * Layouts are still widgets, so they participate in the same tree-based rendering and input model as any
 * other node. Their added responsibility is to position and size child widgets using attached
 * {@link IConstraint constraints}, margins, and padding before those children are drawn.
 * <p>
 * The default implementation is {@link MKLayout}, with specialized subclasses such as
 * {@link MKStackLayoutVertical} and {@link MKStackLayoutHorizontal} providing common stacked arrangements.
 */
public interface IMKLayout extends IMKWidget {

    /**
     * Sets the top margin used by layout constraints.
     *
     * @param value top margin in pixels
     * @return this layout
     */
    IMKLayout setMarginTop(int value);

    /**
     * @return top margin in pixels
     */
    int getMarginTop();

    /**
     * Adds a child widget and attaches the provided constraints in the order given.
     *
     * @param widget the child widget to add
     * @param constraints constraints to apply during layout recomputation
     * @return {@code true} when the widget was added
     */
    default boolean addWidget(IMKWidget widget, IConstraint... constraints) {
        boolean ret = addWidget(widget);
        if (ret) {
            for (IConstraint constraint : constraints) {
                addConstraintToWidget(constraint, widget);
            }
        }
        return ret;
    }

    /**
     * Convenience helper for setting all layout margins at once.
     *
     * @param left left margin
     * @param right right margin
     * @param top top margin
     * @param bottom bottom margin
     * @return this layout
     */
    default IMKLayout setMargins(int left, int right, int top, int bottom) {
        return setMarginLeft(left).setMarginRight(right).setMarginTop(top).setMarginBot(bottom);
    }

    /**
     * Applies layout logic for a single child during a recompute pass.
     * <p>
     * Implementations typically use this to apply the child's registered constraints and any additional
     * container-specific rules based on child order.
     *
     * @param widget the child being laid out
     * @param index the child's index within the layout
     */
    default void layoutWidget(IMKWidget widget, int index) {
    }

    /**
     * Registers a constraint for the given child widget.
     *
     * @param constraint constraint to add
     * @param widget child widget the constraint applies to
     */
    void addConstraintToWidget(IConstraint constraint, IMKWidget widget);

    /**
     * Removes all constraints currently attached to the given child widget.
     *
     * @param widget child widget whose constraints should be removed
     */
    void clearWidgetConstraints(IMKWidget widget);

    /**
     * Removes one specific constraint from a child widget.
     *
     * @param constraint constraint to remove
     * @param widget child widget the constraint currently applies to
     */
    void removeConstraintFromWidget(IConstraint constraint, IMKWidget widget);

    /**
     * Hook invoked before child layout begins.
     */
    default void preLayout() {
    }

    /**
     * Hook invoked after all children have been processed in a recompute pass.
     */
    default void postLayout() {

    }

    /**
     * Convenience helper for setting all child spacing paddings at once.
     *
     * @param left left padding
     * @param right right padding
     * @param top top padding
     * @param bottom bottom padding
     * @return this layout
     */
    default IMKLayout setPaddings(int left, int right, int top, int bottom) {
        return setPaddingLeft(left).setPaddingRight(right).setPaddingTop(top).setPaddingBot(bottom);
    }

    /**
     * Sets the bottom margin used by layout constraints.
     *
     * @param value bottom margin in pixels
     * @return this layout
     */
    IMKLayout setMarginBot(int value);

    /**
     * @return bottom margin in pixels
     */
    int getMarginBot();

    /**
     * Sets the left margin used by layout constraints.
     *
     * @param value left margin in pixels
     * @return this layout
     */
    IMKLayout setMarginLeft(int value);

    /**
     * @return left margin in pixels
     */
    int getMarginLeft();

    /**
     * Sets the right margin used by layout constraints.
     *
     * @param value right margin in pixels
     * @return this layout
     */
    IMKLayout setMarginRight(int value);

    /**
     * @return right margin in pixels
     */
    int getMarginRight();

    /**
     * Sets top padding used when spacing children.
     *
     * @param value top padding in pixels
     * @return this layout
     */
    IMKLayout setPaddingTop(int value);

    /**
     * @return top padding in pixels
     */
    int getPaddingTop();

    /**
     * Sets bottom padding used when spacing children.
     *
     * @param value bottom padding in pixels
     * @return this layout
     */
    IMKLayout setPaddingBot(int value);

    /**
     * @return bottom padding in pixels
     */
    int getPaddingBot();

    /**
     * Sets left padding used when spacing children.
     *
     * @param value left padding in pixels
     * @return this layout
     */
    IMKLayout setPaddingLeft(int value);

    /**
     * @return left padding in pixels
     */
    int getPaddingLeft();

    /**
     * Sets right padding used when spacing children.
     *
     * @param value right padding in pixels
     * @return this layout
     */
    IMKLayout setPaddingRight(int value);

    /**
     * @return right padding in pixels
     */
    int getPaddingRight();

}
