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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record WorkspacePageContext(Font font,
                                   BlockPos anchor,
                                   MKStructureWorkspace workspace,
                                   int screenWidth,
                                   int screenHeight,
                                   int panelWidth,
                                   int panelHeight,
                                   int scrollWidth,
                                   int contentWidth,
                                   int buttonHeight,
                                   int buttonGap,
                                   int bottomPadding,
                                   int topContentY,
                                   int headerScrollGap,
                                   int textColor,
                                   List<String> importManifestIds,
                                   List<String> backupManifestFiles,
                                   Consumer<String> pushState,
                                   Consumer<String> switchToExistingState,
                                   Runnable flagNeedSetup,
                                   WorkspaceFormDraftEditor draftEditor,
                                   PaletteBlockPickerRowAdder addPaletteBlockPickerRow,
                                   Supplier<ResourceLocation> blockSwapSourceBlock,
                                   Consumer<ResourceLocation> setBlockSwapSourceBlock,
                                   Supplier<ResourceLocation> blockSwapTargetBlock,
                                   Consumer<ResourceLocation> setBlockSwapTargetBlock,
                                   BlockPickerRowAdder addBlockPickerRow,
                                   Predicate<MKWorkspacePieceDefinition> supportsStairGeneration,
                                   BiConsumer<MKScrollView, String> finalizeScrollView) {
    @FunctionalInterface
    public interface BlockPickerRowAdder {
        void add(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                 Consumer<ResourceLocation> setter, boolean allowClear);
    }

    @FunctionalInterface
    public interface PaletteBlockPickerRowAdder {
        void add(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                 ResourceLocation defaultBlock, Consumer<ResourceLocation> setter);
    }

    public int panelX() {
        return screenWidth / 2 - panelWidth / 2;
    }

    public int panelY() {
        return screenHeight / 2 - panelHeight / 2;
    }

    public MKText makeWhiteText(Component text) {
        return new MKText(font, text).setColor(textColor);
    }

    public int scrollTopAfterHeader(MKLayout root, MKText headerText) {
        root.manualRecompute();
        return Math.max(root.getY() + topContentY, headerText.getBottom() + headerScrollGap);
    }

}
