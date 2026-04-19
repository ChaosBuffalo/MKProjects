package com.chaosbuffalo.mkwidgets.client.gui.screens;


import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.IInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Screen-level contract for MKWidgets roots.
 * <p>
 * An {@code IMKScreen} owns the top-level widget trees displayed on a Minecraft screen and coordinates the
 * global behaviors that do not belong to any single widget: active modals, keyboard focus, drag state,
 * deferred setup work, and optional named screen states.
 * <p>
 * In the standard {@link MKScreen} implementation, render flow is screen-centric: pre-draw runnables execute
 * first, hover state is refreshed, root widgets are drawn, visible modals are drawn on top, active drag state
 * is updated, and queued post-render instructions such as tooltips are finally rendered above everything else.
 * Input follows a matching precedence order, with modals receiving events before ordinary root widgets.
 * <p>
 * {@link MKScreen} is the standard implementation and is typically the class users subclass when building a
 * full MKWidgets-driven screen.
 */
public interface IMKScreen {

    /**
     * Adds a modal overlay to the screen.
     * <p>
     * Modals render above ordinary root widgets and receive input first while active.
     *
     * @param modal modal overlay to attach
     */
    void addModal(IMKModal modal);

    /**
     * Closes and detaches a modal overlay.
     *
     * @param modal modal overlay to close
     */
    void closeModal(IMKModal modal);

    /**
     * Returns the widget currently receiving focused keyboard input.
     *
     * @return focused widget, or {@code null} if no widget is focused
     */
    IMKWidget getFocus();

    /**
     * Queues a one-shot instruction to draw after the normal widget and modal passes.
     *
     * @param instruction instruction to execute after main rendering
     */
    void addPostRenderInstruction(IInstruction instruction);

    /**
     * Registers a runnable that executes before hover processing and widget drawing each frame.
     * <p>
     * This is useful for inexpensive per-frame state updates that must happen immediately before rendering.
     *
     * @param runnable runnable to execute during the pre-draw phase
     */
    void addPreDrawRunnable(Runnable runnable);

    /**
     * Unregisters a previously added pre-draw runnable.
     *
     * @param runnable runnable to remove
     */
    void removePreDrawRunnable(Runnable runnable);

    /**
     * Removes all registered pre-draw runnables.
     */
    void clearPreDrawRunnables();

    /**
     * Preserves the currently active named state across the next screen rebuild.
     * <p>
     * Implementations typically use this during resize/setup churn so the current state is restored after
     * {@code setupScreen()} rebuilds the state registry.
     */
    void addRestoreStateCallbacks();

    /**
     * Registers a callback to run immediately after the next screen setup pass.
     *
     * @param callback callback to execute after setup
     */
    void addPostSetupCallback(Runnable callback);

    /**
     * Removes a named screen state from the registry.
     *
     * @param name state name to remove
     */
    void removeState(String name);

    /**
     * Starts or replaces the current drag sequence.
     * <p>
     * Drag state is screen-owned instead of widget-owned so drag visuals can render above the full interface and
     * continue updating independently of normal widget tree drawing.
     *
     * @param dragState active drag state implementation
     * @param source widget that originated the drag
     */
    void setDragState(IDragState dragState, IMKWidget source);

    /**
     * Returns the widget that initiated the current drag sequence.
     *
     * @return drag source widget, or {@code null} if no drag is active
     */
    IMKWidget getDragSource();

    /**
     * Returns the current drag state if one is active.
     *
     * @return optional drag state
     */
    Optional<IDragState> getDragState();

    /**
     * Ends the current drag sequence and notifies the drag source, if any.
     */
    void clearDragState();

    /**
     * Registers a lazily created named root state for this screen.
     *
     * @param name state name
     * @param root supplier that builds the root widget for the state
     */
    void addState(String name, Supplier<IMKWidget> root);

    /**
     * Pushes a named state onto the screen's state stack and swaps the active root widget accordingly.
     *
     * @param newState state name to activate
     */
    void pushState(String newState);

    /**
     * Pops the current named state and restores the previous one.
     *
     * @return the state name that was removed, or {@link MKScreen#NO_STATE} if none was active
     */
    String popState();

    /**
     * Returns the currently active state name.
     *
     * @return current state name, or {@link MKScreen#NO_STATE}
     */
    String getState();

    /**
     * Updates the focused widget.
     *
     * @param widget new focused widget, or {@code null} to clear focus
     */
    void setFocus(@Nullable IMKWidget widget);

    /**
     * Adds a root widget to the screen.
     *
     * @param widget root widget to attach
     */
    void addWidget(IMKWidget widget);

    /**
     * Removes a root widget from the screen.
     *
     * @param widget root widget to remove
     */
    void removeWidget(IMKWidget widget);

    /**
     * Returns whether the given widget is currently attached as a root widget.
     *
     * @param widget widget to test
     * @return {@code true} if the widget is attached to the screen root list
     */
    boolean containsWidget(IMKWidget widget);

    /**
     * Clears root widgets, modals, and queued post-render instructions.
     */
    void clear();

    /**
     * Closes and detaches all active modals.
     */
    void clearModals();

    /**
     * Removes and detaches all root widgets.
     */
    void clearWidgets();

    /**
     * @return current screen width in GUI coordinates
     */
    int getWidth();

    /**
     * @return current screen height in GUI coordinates
     */
    int getHeight();

}
