package com.chaosbuffalo.mknpc.world.gen.workspace.stairs;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceStairBuilderTest {
    @Test
    void capGenerationGeometryUsesExpectedTraversalRange() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();

        MKWorkspaceVerticalAccessGeometry.ShaftGeometry topGeometry = builder.getGenerationGeometry(workspace,
                verticalCapPiece(Direction.DOWN, MKWorkspaceVerticalAccessTags.TOP_CAP_TAG));
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry bottomGeometry = builder.getGenerationGeometry(workspace,
                verticalCapPiece(Direction.UP, MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG));

        assertEquals(0, topGeometry.interiorMinY());
        assertEquals(0, topGeometry.interiorMaxY());
        assertEquals(0, bottomGeometry.interiorMinY());
        assertEquals(6, bottomGeometry.interiorMaxY());
    }

    @Test
    void bottomCapKeepsFullPhaseGeometryButClipsEditsAboveBottomShell() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspacePieceDefinition bottomCap = verticalCapPiece(Direction.UP,
                MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG);
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace, bottomCap);

        assertEquals(0, geometry.interiorMinY());
        assertEquals(6, geometry.interiorMaxY());
        assertEquals(1, builder.getEditableMinY(bottomCap, geometry));
    }

    private MKWorkspacePieceDefinition verticalCapPiece(Direction connectorFacing, String capTag) {
        UUID workspaceId = UUID.randomUUID();
        BoundingBox bounds = new BoundingBox(0, 0, 0, 10, 6, 10);
        Map<String, String> tags = Map.of(
                MKWorkspaceVerticalAccessTags.ENABLED_TAG, "true",
                capTag, "true"
        );
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                "cap",
                MKWorkspacePieceRole.TOP_CAP,
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(verticalConnector(connectorFacing)),
                BlockPos.ZERO,
                bounds,
                bounds,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private MKWorkspaceConnectorDefinition verticalConnector(Direction facing) {
        ResourceLocation empty = ResourceLocation.parse("minecraft:empty");
        return new MKWorkspaceConnectorDefinition(
                facing == Direction.UP ? MKConnectorRole.CONNECT_UP : MKConnectorRole.CONNECT_DOWN,
                facing,
                BlockPos.ZERO,
                3,
                3,
                0,
                0,
                empty,
                empty,
                empty,
                empty
        );
    }
}
