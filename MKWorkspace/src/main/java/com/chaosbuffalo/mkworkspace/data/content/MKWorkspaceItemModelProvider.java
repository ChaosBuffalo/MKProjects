package com.chaosbuffalo.mkworkspace.data.content;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MKWorkspaceItemModelProvider extends ItemModelProvider {
    public MKWorkspaceItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MKWorkspace.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        getBuilder("mk_workspace_dev")
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/mk_workspace_dev")));

        withExistingParent("mk_workspace_insert_tool", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(MKWorkspace.MODID,
                        "item/workspace_insert_tool"));
    }
}
