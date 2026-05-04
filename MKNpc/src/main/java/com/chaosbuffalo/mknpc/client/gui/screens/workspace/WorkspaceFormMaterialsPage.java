package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import net.minecraft.network.chat.Component;

public class WorkspaceFormMaterialsPage extends WorkspacePageBase {
    public static final String ID = "form_materials";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Materials"));
        addHeaderText(screen, root, Component.literal(
                "Edit the room shell and stair palette without the rest of the layout controls in view."));

        int rowTop = screen.panelY() + 96;
        MKWorkspaceMaterialPalette defaultPalette = MKWorkspaceMaterialPalette.defaultPalette();
        WorkspaceDraftSession editor = screen.draftSession();
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop, "Floor",
                editor.floorBlock(), defaultPalette.floorBlock(), editor::floorBlock);
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop + 34, "Wall",
                editor.wallBlock(), defaultPalette.wallBlock(), editor::wallBlock);
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop + 68, "Ceiling",
                editor.ceilingBlock(), defaultPalette.ceilingBlock(), editor::ceilingBlock);
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop + 102, "Stair",
                editor.stairBlock(), defaultPalette.stairBlock(), editor::stairBlock);
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop + 136, "Slab",
                editor.slabBlock(), defaultPalette.slabBlock(), editor::slabBlock);
        screen.addPaletteBlockPickerRow(root, screen.panelX(), rowTop + 170, "Ladder",
                editor.ladderBlock(), defaultPalette.ladderBlock(), editor::ladderBlock);

        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }
}


