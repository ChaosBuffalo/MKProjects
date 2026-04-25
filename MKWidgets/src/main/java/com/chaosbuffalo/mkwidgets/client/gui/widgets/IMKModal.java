package com.chaosbuffalo.mkwidgets.client.gui.widgets;

/**
 * Marker interface for widgets that behave as screen-wide modal overlays.
 */
public interface IMKModal extends IMKWidget {

    /**
     * Configures whether clicking outside modal content should close the modal.
     *
     * @param value {@code true} to close the modal on outside click
     * @return this modal
     */
    IMKModal setCloseOnClickOutside(boolean value);

    /**
     * @return {@code true} when an outside click should close the modal
     */
    boolean shouldCloseOnClickOutside();

    /**
     * Registers a callback to run when the modal is closed by the screen.
     *
     * @param callback close callback
     * @return this modal
     */
    IMKModal setOnCloseCallback(Runnable callback);

    /**
     * @return the close callback, or {@code null} if none is registered
     */
    Runnable getOnCloseCallback();

}
