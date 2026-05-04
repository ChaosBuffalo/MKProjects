package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface WorkspaceFormDraftEditor {
    String summary();

    String workspaceId();

    boolean hasExistingWorkspacePieces();

    void submit();

    void send();

    String namespace();

    void namespace(String value);

    String structureName();

    void structureName(String value);

    int shellMargin();

    void shellMargin(int value);

    int exteriorAirMargin();

    void exteriorAirMargin(int value);

    int previewMargin();

    void previewMargin(int value);

    ResourceLocation floorBlock();

    void floorBlock(ResourceLocation value);

    ResourceLocation wallBlock();

    void wallBlock(ResourceLocation value);

    ResourceLocation ceilingBlock();

    void ceilingBlock(ResourceLocation value);

    ResourceLocation stairBlock();

    void stairBlock(ResourceLocation value);

    ResourceLocation slabBlock();

    void slabBlock(ResourceLocation value);

    ResourceLocation ladderBlock();

    void ladderBlock(ResourceLocation value);

    long familyCount(MKTowerWorkspaceCategory category);

    void selectedFamilyCategory(MKTowerWorkspaceCategory category);

    List<MKHorizontalOpeningProfile> openingProfiles();

    void selectedOpeningIndex(int index);

    int addOpeningProfile();
}
