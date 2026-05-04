package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Materials"));
        addHeaderText(context, root, Component.literal(
                "Edit the room shell and stair palette without the rest of the layout controls in view."));

        int rowTop = context.panelY() + 96;
        MKWorkspaceMaterialPalette defaultPalette = MKWorkspaceMaterialPalette.defaultPalette();
        WorkspaceFormDraftEditor editor = context.draftEditor();
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop, "Floor",
                editor.floorBlock(), defaultPalette.floorBlock(), editor::floorBlock);
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 34, "Wall",
                editor.wallBlock(), defaultPalette.wallBlock(), editor::wallBlock);
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 68, "Ceiling",
                editor.ceilingBlock(), defaultPalette.ceilingBlock(), editor::ceilingBlock);
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 102, "Stair",
                editor.stairBlock(), defaultPalette.stairBlock(), editor::stairBlock);
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 136, "Slab",
                editor.slabBlock(), defaultPalette.slabBlock(), editor::slabBlock);
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 170, "Ladder",
                editor.ladderBlock(), defaultPalette.ladderBlock(), editor::ladderBlock);

        addBackButton(context, root, WorkspaceFormPage.ID);
        return root;
    }
}
