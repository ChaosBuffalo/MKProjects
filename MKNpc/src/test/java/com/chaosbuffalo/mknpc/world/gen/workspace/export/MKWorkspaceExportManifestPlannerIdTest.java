package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinitionPlannerIdTest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceExportManifestPlannerIdTest {
    @Test
    void exportPieceIncludesPlannerId() {
        MKWorkspacePlannerId plannerId = MKWorkspacePlannerId.of(
                "keep.main.tower.center.floor.basement_01.floor_plan.room.main_00");
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition piece = MKWorkspacePieceDefinitionPlannerIdTest.piece(plannerId);

        MKWorkspaceExportManifest.ExportPiece exportPiece = MKWorkspaceExportManifest.ExportPiece.from(workspace, piece);
        JsonElement encoded = MKWorkspaceExportManifest.ExportPiece.CODEC.encodeStart(JsonOps.INSTANCE, exportPiece)
                .getOrThrow();
        MKWorkspaceExportManifest.ExportPiece decoded = MKWorkspaceExportManifest.ExportPiece.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(plannerId.value(), exportPiece.plannerId());
        assertEquals(plannerId.value(), encoded.getAsJsonObject().get("planner_id").getAsString());
        assertEquals(plannerId.value(), decoded.plannerId());
    }
}
