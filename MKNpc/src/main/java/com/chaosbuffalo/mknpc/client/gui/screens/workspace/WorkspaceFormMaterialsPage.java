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
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop, "Floor",
                context.draftFloorBlock().get(), defaultPalette.floorBlock(), context.setDraftFloorBlock());
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 34, "Wall",
                context.draftWallBlock().get(), defaultPalette.wallBlock(), context.setDraftWallBlock());
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 68, "Ceiling",
                context.draftCeilingBlock().get(), defaultPalette.ceilingBlock(), context.setDraftCeilingBlock());
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 102, "Stair",
                context.draftStairBlock().get(), defaultPalette.stairBlock(), context.setDraftStairBlock());
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 136, "Slab",
                context.draftSlabBlock().get(), defaultPalette.slabBlock(), context.setDraftSlabBlock());
        context.addPaletteBlockPickerRow().add(root, context.panelX(), rowTop + 170, "Ladder",
                context.draftLadderBlock().get(), defaultPalette.ladderBlock(), context.setDraftLadderBlock());

        addBackButton(context, root, WorkspaceFormPage.ID);
        return root;
    }
}
