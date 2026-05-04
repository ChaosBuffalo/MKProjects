package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public interface WorkspacePageContext {
    Font font();

    BlockPos anchor();

    MKStructureWorkspace workspace();

    int screenWidth();

    int screenHeight();

    int panelWidth();

    int panelHeight();

    int scrollWidth();

    int contentWidth();

    int buttonHeight();

    int buttonGap();

    int bottomPadding();

    int topContentY();

    int headerScrollGap();

    int textColor();

    List<String> importManifestIds();

    List<String> backupManifestFiles();

    WorkspaceCategoryEditor categoryEditor();

    WorkspaceFormDraftEditor draftEditor();

    ResourceLocation blockSwapSourceBlock();

    void setBlockSwapSourceBlock(ResourceLocation value);

    ResourceLocation blockSwapTargetBlock();

    void setBlockSwapTargetBlock(ResourceLocation value);

    void closeScreen();

    void pushState(String state);

    void switchToExistingState(String state);

    void flagNeedSetup();

    void openWorkspaceCategory(String topologyKey);

    boolean supportsStairGeneration(MKWorkspacePieceDefinition piece);

    void finalizeScrollView(MKScrollView scrollView, String stateName);

    void addBlockPickerRow(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                           Consumer<ResourceLocation> setter, boolean allowClear);

    void addPaletteBlockPickerRow(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                                  ResourceLocation defaultBlock, Consumer<ResourceLocation> setter);

    default int panelX() {
        return screenWidth() / 2 - panelWidth() / 2;
    }

    default int panelY() {
        return screenHeight() / 2 - panelHeight() / 2;
    }

    default MKText makeWhiteText(Component text) {
        return new MKText(font(), text).setColor(textColor());
    }

    default int scrollTopAfterHeader(MKLayout root, MKText headerText) {
        root.manualRecompute();
        return Math.max(root.getY() + topContentY(), headerText.getBottom() + headerScrollGap());
    }
}
