package com.chaosbuffalo.mkworkspace.data.content;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MKWorkspaceLanguageProvider extends LanguageProvider {
    public MKWorkspaceLanguageProvider(PackOutput output, String locale) {
        super(output, MKWorkspace.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("block.mkworkspace.mk_workspace_dev", "Workspace Dev Block");
        add("item.mkworkspace.mk_workspace_dev", "Workspace Dev Block");
        add("item.mkworkspace.mk_workspace_insert_tool", "Workspace Insert Tool");

        add("mknpc.workspace.screen.title", "Tower Workspace Authoring");
        add("mknpc.workspace.screen.help", "Configure a tower template workspace. Dimensions should stay odd for centered connectors. Linear runs can be as tight as 1 wide and 2 high.");
        add("mknpc.workspace.screen.generate", "Create + Generate");
        add("mknpc.workspace.screen.manage_title", "Tower Workspace Pieces");
        add("mknpc.workspace.screen.manage_summary", "Workspace %s:%s currently has %s generated pieces.");
        add("mknpc.workspace.section.header", "%s (%s)");
        add("mknpc.workspace.button.add_copy", "Add Copy");
        add("mknpc.workspace.button.add_copy_for_all", "Add Variant For All Pieces");
        add("mknpc.workspace.button.export_all", "Export All Structure Pieces");
        add("mknpc.workspace.button.close", "Close");
        add("mknpc.workspace.button.edit_template_settings", "Edit Template Settings");
        add("mknpc.workspace.button.back_to_workspace", "Back To Workspace");
        add("mknpc.workspace.message.exported_pieces", "Exported %s workspace structure pieces and %s metadata files to archive %s.");
        add("mknpc.workspace.message.export_failed", "Workspace export failed.");
        add("mknpc.workspace.field.namespace", "Namespace");
        add("mknpc.workspace.field.structure_name", "Structure Name");
        add("mknpc.workspace.field.room_width", "Room Width");
        add("mknpc.workspace.field.room_length", "Room Length");
        add("mknpc.workspace.field.entrance_height", "Entrance Height");
        add("mknpc.workspace.field.room_height", "Room Height");
        add("mknpc.workspace.field.hall_width", "Vertical Shaft Size");
        add("mknpc.workspace.field.door_width", "Door Width");
        add("mknpc.workspace.field.door_height", "Door Height");
        add("mknpc.workspace.field.shell_margin", "Shell Margin");
        add("mknpc.workspace.field.exterior_air_margin", "Exterior Air Margin");
        add("mknpc.workspace.field.stair_placement", "Stair Placement");
        add("mknpc.workspace.field.preview_margin", "Preview Margin");
        add("mknpc.workspace.field.floor_block", "Floor Block");
        add("mknpc.workspace.field.wall_block", "Wall Block");
        add("mknpc.workspace.field.ceiling_block", "Ceiling Block");
        add("mknpc.workspace.field.hotbar", "Hotbar");
        add("mknpc.workspace.stair_placement.center", "Center");
        add("mknpc.workspace.stair_placement.north", "North");
        add("mknpc.workspace.stair_placement.south", "South");
        add("mknpc.workspace.stair_placement.east", "East");
        add("mknpc.workspace.stair_placement.west", "West");
    }
}
