package com.chaosbuffalo.mkwidgets.client.gui.screens;


import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.IInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public interface IMKScreen {

    void addModal(IMKModal modal);

    void closeModal(IMKModal modal);

    IMKWidget getFocus();

    void addPostRenderInstruction(IInstruction instruction);

    void addPreDrawRunnable(Runnable runnable);

    void removePreDrawRunnable(Runnable runnable);

    void clearPreDrawRunnables();

    void addRestoreStateCallbacks();

    void addPostSetupCallback(Runnable callback);

    void removeState(String name);

    void setDragState(IDragState dragState, IMKWidget source);

    IMKWidget getDragSource();

    Optional<IDragState> getDragState();

    void clearDragState();

    void addState(String name, Supplier<IMKWidget> root);

    void pushState(String newState);

    String popState();

    String getState();

    void setFocus(@Nullable IMKWidget widget);

    void addWidget(IMKWidget widget);

    void removeWidget(IMKWidget widget);

    boolean containsWidget(IMKWidget widget);

    void clear();

    void clearModals();

    void clearWidgets();

    int getWidth();

    int getHeight();

}
