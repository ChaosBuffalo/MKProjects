package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceDefinitionSummaryBuilderTest {
    @Test
    void fullRegenerationInvalidatesEveryGeneratedLayer() {
        MKWorkspaceMutationPreflight preflight = new MKWorkspaceMutationPreflight(
                MKWorkspaceInvalidationReport.noChanges("No analyzer invalidations"), null);

        assertEquals(Arrays.asList(MKWorkspaceGeneratedLayer.values()),
                MKWorkspaceDefinitionSummaryBuilder.invalidatedLayers(preflight,
                        MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE));
    }

    @Test
    void codecRoundTripDoesNotInventIdentityBasedFieldChanges() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKStructureWorkspace decoded = MKStructureWorkspace.CODEC.parse(JsonOps.INSTANCE,
                MKStructureWorkspace.CODEC.encodeStart(JsonOps.INSTANCE, workspace).getOrThrow()).getOrThrow();

        assertEquals(List.of(), MKWorkspaceDefinitionFieldDiff.differences(workspace, decoded));
    }

    @Test
    void changedModelObjectsProduceHumanReadableFieldDescriptions() {
        MKStructureWorkspace before = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceDimensions dimensions = before.dimensions();
        MKWorkspaceDimensions changedDimensions = new MKWorkspaceDimensions(
                dimensions.roomWidth() + 2, dimensions.roomLength(), dimensions.entranceHeight(),
                dimensions.roomHeight(), dimensions.basementHeight(), dimensions.shaftWidth(),
                dimensions.doorwayWidth(), dimensions.doorwayHeight());
        ArrayList<MKHorizontalOpeningProfile> changedProfiles = new ArrayList<>(before.openingProfiles());
        MKHorizontalOpeningProfile profile = changedProfiles.getFirst();
        changedProfiles.set(0, new MKHorizontalOpeningProfile(profile.profileId(), profile.openingWidth() + 2,
                profile.openingHeight(), profile.allowOnMainPath(), profile.allowOnBranchPath()));
        MKStructureWorkspace after = copyWith(before, changedDimensions, changedProfiles);

        List<MKWorkspaceFieldChange> fields = MKWorkspaceDefinitionFieldDiff.differences(before, after);

        MKWorkspaceFieldChange width = fields.stream()
                .filter(field -> field.field().equals("dimensions.room width")).findFirst().orElseThrow();
        assertEquals(Integer.toString(dimensions.roomWidth()), width.beforeValue());
        assertEquals(Integer.toString(dimensions.roomWidth() + 2), width.afterValue());
        MKWorkspaceFieldChange opening = fields.stream()
                .filter(field -> field.field().equals("opening profile " + profile.profileId()))
                .findFirst().orElseThrow();
        assertTrue(opening.beforeValue().startsWith("opening="));
        assertTrue(opening.afterValue().contains("allowed paths="));
        assertFalse(fields.stream().flatMap(field -> java.util.stream.Stream.of(
                        field.beforeValue(), field.afterValue()))
                .anyMatch(value -> value.contains("com.chaosbuffalo") || value.matches(".*@[0-9a-fA-F]+.*")));
    }

    private MKStructureWorkspace copyWith(MKStructureWorkspace workspace, MKWorkspaceDimensions dimensions,
                                          List<MKHorizontalOpeningProfile> openingProfiles) {
        return new MKStructureWorkspace(workspace.id(), workspace.anchor(), workspace.namespace(),
                workspace.structureName(), workspace.topologyProfile(), dimensions, workspace.palette(),
                workspace.stairConfig(), workspace.verticalAccessPlacement(), workspace.shellMargin(),
                workspace.verticalShellMargin(), workspace.exteriorAirMargin(), workspace.previewMargin(),
                workspace.verticalAccessSpec(), workspace.familyDefinitions(), openingProfiles,
                workspace.linearRunFamilies(), workspace.insertFamilies(), workspace.createdAt(),
                workspace.updatedAt(), workspace.pieces(), workspace.layerStates());
    }
}
