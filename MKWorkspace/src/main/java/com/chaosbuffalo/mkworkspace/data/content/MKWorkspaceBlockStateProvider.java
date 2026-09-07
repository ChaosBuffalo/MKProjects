package com.chaosbuffalo.mkworkspace.data.content;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.init.MKWorkspaceBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MKWorkspaceBlockStateProvider extends BlockStateProvider {
    public MKWorkspaceBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MKWorkspace.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile workspaceDevModel = cubeAll(MKWorkspaceBlocks.MK_WORKSPACE_DEV_BLOCK.get());
        simpleBlock(MKWorkspaceBlocks.MK_WORKSPACE_DEV_BLOCK.get(), workspaceDevModel);
    }
}
