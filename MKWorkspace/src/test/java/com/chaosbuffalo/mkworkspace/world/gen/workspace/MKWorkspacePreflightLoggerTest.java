package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePreflightLoggerTest {
    @Test
    void describesConfirmEffectsWithTemplateImpactsAndRemaps() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(new BlockPos(1, 2, 3));
        MKWorkspaceTemplateRemapSuggestion remap = new MKWorkspaceTemplateRemapSuggestion(
                MKWorkspacePlannerId.of("old.room"),
                MKWorkspacePlannerId.of("new.room"),
                90,
                "same stable slot");
        MKWorkspaceInvalidationReport report = new MKWorkspaceInvalidationReport(
                List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS, MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS),
                List.of(MKWorkspacePlannerId.of("new.room")),
                List.of(),
                List.of(MKWorkspacePlannerId.of("old.room")),
                MKWorkspaceMutationSafety.SAFE_RELAYOUT,
                "Workspace catalog relayout will preserve matched physical authored templates.",
                "preserve_catalog_relayout",
                List.of("2 physical authored templates will be preserved."),
                List.of(remap),
                List.of(
                        new MKWorkspaceRelayoutImpact(
                                "preserved",
                                "main_room_template",
                                "main_room",
                                0,
                                "new.room",
                                "stable:floor.main:0",
                                "authored blocks stay in the current catalog position"),
                        new MKWorkspaceRelayoutImpact(
                                "removed",
                                "old_room_1",
                                "old_room",
                                1,
                                "old.room.variant_1",
                                "stable:floor.old:1",
                                "physical authored template slot is not present in the target catalog")
                )
        );
        MKWorkspaceMutationPreflight preflight = new MKWorkspaceMutationPreflight(report, workspace);

        List<String> lines = new MKWorkspacePreflightLogger()
                .describeConfirmEffects("confirm", "Tester", workspace, preflight, List.of(remap));

        assertTrue(lines.stream().anyMatch(line -> line.contains("phase=confirm") &&
                line.contains("player=Tester") &&
                line.contains("operation=preserve_catalog_relayout")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("invalidates: template_bindings, scaffold_blocks")),
                lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("accepted remap: orphaned=old.room target=new.room")),
                lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("suggested remap: orphaned=old.room target=new.room")),
                lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("template impact: total=2 preserved=1") &&
                line.contains("removed=1")), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.contains("template impact detail: outcome=removed") &&
                line.contains("piece=old_room_1") &&
                line.contains("variant=1")), lines.toString());
    }
}
