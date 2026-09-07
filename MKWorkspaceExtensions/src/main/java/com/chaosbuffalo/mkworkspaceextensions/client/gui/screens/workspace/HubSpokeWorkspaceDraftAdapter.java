package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerDraftAdapter;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HubSpokeWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    @Override
    public ResourceLocation plannerId() {
        return HubSpokePlanner.PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        return HubSpokePlanner.defaultTopologyProfile();
    }

    @Override
    public String primaryDimensionStackId() {
        return HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID;
    }

    @Override
    public boolean usesPrimaryDimensionStack() {
        return false;
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.draft().familyDefinitions = session.draft().familyDefinitions.stream()
                .map(family -> copyWithHeight(family, requestedHeight))
                .toList();
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        session.draft().topologyProfile = HubSpokePlanner.defaultTopologyProfile();
        session.draft().familyDefinitions = HubSpokePlanner.defaultRoomFamilyDefinitions(dimensions);
        session.draft().linearRunFamilies = List.of();
        session.draft().insertFamilies = List.of();
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        boolean hasHubSpokeFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("hub_spoke."));
        if (!hasHubSpokeFamilies) {
            session.draft().familyDefinitions = HubSpokePlanner.defaultRoomFamilyDefinitions(dimensions);
        }
        session.draft().topologyProfile = HubSpokePlannerSettings.from(session.draft().topologyProfile)
                .applyTo(session.draft().topologyProfile);
        ensureSpokeFamiliesForActiveSlots(session);
        ensureCornerFamiliesForActiveSlots(session);
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .filter(linearRun -> !linearRun.topologySlotId().startsWith("hub_spoke."))
                .toList();
    }

    @Override
    public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (HubSpokePlanner.SPOKE_SLOT.equals(topologySlotId)) {
            return HubSpokePlanner.usesSharedSpokeSlot(settings);
        }
        if (HubSpokePlanner.concreteSpokeSlots().contains(topologySlotId)) {
            return HubSpokePlanner.sourceSpokeSlots(settings).contains(topologySlotId);
        }
        if (HubSpokePlanner.CORNER_SLOT.equals(topologySlotId)) {
            return settings.anySharedCorner();
        }
        if (HubSpokePlanner.concreteCornerSlots().contains(topologySlotId)) {
            return settings.uniqueCorner(topologySlotId);
        }
        return true;
    }

    @Override
    public Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return Optional.empty();
    }

    @Override
    public List<String> templateBaseNamesForFamily(WorkspaceDraftSession session,
                                                   MKWorkspaceRoomFamilyDefinition family) {
        if (HubSpokePlanner.SPOKE_SLOT.equals(family.topologySlotId()) &&
                HubSpokePlanner.SPOKE_BASE_NAME.equals(family.baseName())) {
            return List.of(family.baseName(), HubSpokePlanner.SPOKE_BASE_NAME + "_north");
        }
        if (HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()) &&
                HubSpokePlanner.CORNER_BASE_NAME.equals(family.baseName())) {
            return List.of(family.baseName(), HubSpokePlanner.CORNER_BASE_NAME + "_north_west");
        }
        return WorkspacePlannerDraftAdapter.super.templateBaseNamesForFamily(session, family);
    }

    @Override
    public Optional<MKWorkspaceRoomFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                        String topologySlotId) {
        if (HubSpokePlanner.concreteSpokeSlots().contains(topologySlotId)) {
            return session.draft().familyDefinitions.stream()
                    .filter(family -> HubSpokePlanner.SPOKE_SLOT.equals(family.topologySlotId()))
                    .findFirst();
        }
        if (!HubSpokePlanner.concreteCornerSlots().contains(topologySlotId)) {
            return Optional.empty();
        }
        return session.draft().familyDefinitions.stream()
                .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                .findFirst();
    }

    @Override
    public Optional<Integer> addFamilyDefinition(WorkspaceDraftSession session,
                                                 com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema slot) {
        if (!isSpokeTemplateSlot(slot.slotId())) {
            return Optional.empty();
        }
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (settings.spokeTemplates().size() >= HubSpokePlannerSettings.MAX_SPOKE_TEMPLATES) {
            return Optional.of(firstFamilyIndexForSlot(session, slot.slotId()).orElse(-1));
        }
        HubSpokePlannerSettings.SpokeTemplate sourceTemplate = sourceSpokeTemplate(settings, slot.slotId())
                .orElse(settings.spokeTemplates().getLast());
        Set<String> existingBaseNames = settings.spokeTemplates().stream()
                .map(HubSpokePlannerSettings.SpokeTemplate::baseName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        HubSpokePlannerSettings updatedSettings = settings.withAddedSpokeTemplate(sourceTemplate);
        String addedBaseName = updatedSettings.spokeTemplates().stream()
                .map(HubSpokePlannerSettings.SpokeTemplate::baseName)
                .filter(baseName -> !existingBaseNames.contains(baseName))
                .findFirst()
                .orElse("");
        applySettings(session, updatedSettings);
        ensureSpokeFamiliesForActiveSlots(session);
        return familyIndexForSpokeTemplateSource(session, addedBaseName)
                .or(() -> firstFamilyIndexForSlot(session, slot.slotId()))
                .or(() -> Optional.of(session.draft().familyDefinitions.isEmpty() ?
                        -1 : session.draft().familyDefinitions.size() - 1));
    }

    @Override
    public Optional<Integer> addFamilyDefinitionFromPiece(WorkspaceDraftSession session,
                                                          com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema slot,
                                                          MKWorkspacePieceDefinition sourcePiece) {
        String archetypeSlot = sourcePiece.tags().get(HubSpokePlanner.HUB_SPOKE_ARCHETYPE_SLOT_TAG);
        if (HubSpokePlanner.SPOKE_SLOT.equals(archetypeSlot)) {
            return addSpokeFamilyFromPiece(session, slot.slotId(), sourcePiece);
        }
        if (HubSpokePlanner.CORNER_SLOT.equals(archetypeSlot)) {
            return addCornerFamilyFromPiece(session, sourcePiece);
        }
        return Optional.empty();
    }

    @Override
    public boolean removeFamilyDefinition(WorkspaceDraftSession session, int index) {
        if (index < 0 || index >= session.draft().familyDefinitions.size()) {
            return false;
        }
        MKWorkspaceRoomFamilyDefinition family = session.draft().familyDefinitions.get(index);
        if (!isSpokeTemplateSlot(family.topologySlotId())) {
            return false;
        }
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (settings.spokeTemplates().size() <= 1) {
            return true;
        }
        Optional<String> templateBaseName = templateBaseNameForFamily(settings, family)
                .map(HubSpokePlannerSettings.SpokeTemplate::baseName);
        if (templateBaseName.isEmpty()) {
            return false;
        }
        int templateIndex = -1;
        for (int i = 0; i < settings.spokeTemplates().size(); i++) {
            if (templateBaseName.get().equals(settings.spokeTemplates().get(i).baseName())) {
                templateIndex = i;
                break;
            }
        }
        if (templateIndex < 0) {
            return false;
        }
        HubSpokePlannerSettings updatedSettings = settings.withRemovedSpokeTemplate(templateIndex);
        applySettings(session, updatedSettings);
        removeFamilyAndInactiveSpokeSources(session, index, updatedSettings);
        ensureSpokeFamiliesForActiveSlots(session);
        return true;
    }

    @Override
    public int maxRoomHeightForFamilyNormalization(WorkspaceDraftSession session, Optional<String> verticalStackId) {
        return verticalStackId
                .map(stackId -> session.draft().topologyProfile.verticalStackSettingsOrDefault(stackId).height())
                .orElse(HubSpokePlanner.MAX_PLATFORM_HEIGHT);
    }

    private boolean isSpokeTemplateSlot(String topologySlotId) {
        return HubSpokePlanner.SPOKE_SLOT.equals(topologySlotId) ||
                HubSpokePlanner.concreteSpokeSlots().contains(topologySlotId);
    }

    private Optional<Integer> addSpokeFamilyFromPiece(WorkspaceDraftSession session, String topologySlotId,
                                                      MKWorkspacePieceDefinition sourcePiece) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (settings.spokeTemplates().size() >= HubSpokePlannerSettings.MAX_SPOKE_TEMPLATES) {
            return Optional.of(firstFamilyIndexForSlot(session, topologySlotId).orElse(-1));
        }
        HubSpokePlannerSettings.SpokeTemplate sourceTemplate = sourceSpokeTemplateForPiece(settings, sourcePiece)
                .or(() -> sourceSpokeTemplate(settings, topologySlotId))
                .orElse(settings.spokeTemplates().getLast());
        Direction sourceDirection = directionForSourcePiece(sourcePiece)
                .or(() -> HubSpokePlanner.concreteSpokeDirection(topologySlotId))
                .orElse(Direction.NORTH);
        int length = sourceDirection.getAxis() == Direction.Axis.X ?
                sourcePiece.effectiveDimensions().roomWidth() :
                sourcePiece.effectiveDimensions().roomLength();
        HubSpokePlannerSettings.SpokeTemplate sourceWithClickedDimensions =
                new HubSpokePlannerSettings.SpokeTemplate(
                        sourceTemplate.baseName(),
                        sourceTemplate.label(),
                        length,
                        sourcePiece.effectiveDimensions().roomHeight(),
                        sourceTemplate.validDirections());
        Set<String> existingBaseNames = settings.spokeTemplates().stream()
                .map(HubSpokePlannerSettings.SpokeTemplate::baseName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        HubSpokePlannerSettings updatedSettings = settings.withAddedSpokeTemplate(sourceWithClickedDimensions);
        String addedBaseName = updatedSettings.spokeTemplates().stream()
                .map(HubSpokePlannerSettings.SpokeTemplate::baseName)
                .filter(baseName -> !existingBaseNames.contains(baseName))
                .findFirst()
                .orElse("");
        applySettings(session, updatedSettings);
        ensureSpokeFamiliesForActiveSlots(session);
        return familyIndexForSpokeTemplateSource(session, addedBaseName)
                .or(() -> firstFamilyIndexForSlot(session, topologySlotId));
    }

    private Optional<Integer> addCornerFamilyFromPiece(WorkspaceDraftSession session,
                                                       MKWorkspacePieceDefinition sourcePiece) {
        String topologySlotId = sourcePiece.tags().getOrDefault(HubSpokePlanner.HUB_SPOKE_CONCRETE_SLOT_TAG,
                sourcePiece.tags().getOrDefault("workspace_topology_slot_id", ""));
        if (!HubSpokePlanner.concreteCornerSlots().contains(topologySlotId)) {
            return Optional.empty();
        }
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        applySettings(session, settings.withCornerMode(topologySlotId, true));
        ensureCornerFamiliesForActiveSlots(session);
        MKWorkspaceRoomFamilyDefinition shared = session.draft().familyDefinitions.stream()
                .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                .findFirst()
                .or(() -> session.draft().familyDefinitions.stream()
                        .filter(family -> topologySlotId.equals(family.topologySlotId()))
                        .findFirst())
                .orElseGet(() -> HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions())
                        .stream()
                        .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                        .findFirst()
                        .orElseThrow());
        MKWorkspaceRoomFamilyDefinition family = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                session.nextUniqueFamilyBaseName(sourcePiece.tags().getOrDefault("workspace_base_name",
                        sourcePiece.pieceName())),
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, "corner", "room", true),
                shared.verticalAccessGroupId(),
                shared.supportsVerticalAccess(),
                sourcePiece.effectiveDimensions().roomWidth(),
                sourcePiece.effectiveDimensions().roomLength(),
                sourcePiece.effectiveDimensions().roomHeight(),
                shared.horizontalExtrusionMode(),
                shared.horizontalExits(),
                shared.topVoidMargin(),
                shared.bottomVoidMargin(),
                shared.foundationPolicyOverride(),
                shared.paletteOverride()
        ).withTemplateCloneSourcePieceName(sourcePiece.pieceName());
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new ArrayList<>(session.draft().familyDefinitions);
        updated.add(session.normalizeFamilyDefinition(family));
        session.draft().familyDefinitions = List.copyOf(updated);
        return Optional.of(updated.size() - 1);
    }

    private Optional<HubSpokePlannerSettings.SpokeTemplate> sourceSpokeTemplate(HubSpokePlannerSettings settings,
                                                                                String topologySlotId) {
        if (HubSpokePlanner.concreteSpokeSlots().contains(topologySlotId)) {
            return settings.spokeTemplates().stream()
                    .filter(template -> HubSpokePlanner.sourceSpokeSlot(template)
                            .map(topologySlotId::equals)
                            .orElse(false))
                    .reduce((ignored, last) -> last)
                    .or(() -> HubSpokePlanner.concreteSpokeDirection(topologySlotId)
                            .flatMap(direction -> settings.spokeTemplates().stream()
                                    .filter(template -> template.validDirections().contains(direction))
                                    .reduce((ignored, last) -> last)));
        }
        return settings.spokeTemplates().isEmpty() ? Optional.empty() : Optional.of(settings.spokeTemplates().getLast());
    }

    private Optional<HubSpokePlannerSettings.SpokeTemplate> sourceSpokeTemplateForPiece(
            HubSpokePlannerSettings settings,
            MKWorkspacePieceDefinition sourcePiece) {
        String sourceBaseName = sourcePiece.tags().getOrDefault("workspace_base_name", sourcePiece.pieceName());
        return settings.spokeTemplates().stream()
                .filter(template -> sourceBaseName.equals(template.baseName()) ||
                        template.validDirections().stream()
                                .map(template::pieceName)
                                .anyMatch(sourceBaseName::equals))
                .findFirst();
    }

    private Optional<Direction> directionForSourcePiece(MKWorkspacePieceDefinition sourcePiece) {
        String directionName = sourcePiece.tags().get(HubSpokePlanner.HUB_SPOKE_DIRECTION_TAG);
        if (directionName == null || directionName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(Direction.byName(directionName));
    }

    private Optional<Integer> familyIndexForSpokeTemplateSource(WorkspaceDraftSession session, String baseName) {
        if (baseName.isBlank()) {
            return Optional.empty();
        }
        return session.familyDefinitions().stream()
                .filter(family -> family.baseName().startsWith(baseName + "_"))
                .findFirst()
                .flatMap(family -> session.familyIndexesForTopologySlot(family.topologySlotId()).stream()
                        .filter(index -> session.familyDefinitions().get(index).baseName().equals(family.baseName()))
                        .findFirst());
    }

    private Optional<HubSpokePlannerSettings.SpokeTemplate> templateBaseNameForFamily(HubSpokePlannerSettings settings,
                                                                                      MKWorkspaceRoomFamilyDefinition family) {
        return settings.spokeTemplates().stream()
                .filter(template -> HubSpokePlanner.sourceSpokeDirection(template)
                        .map(direction -> template.pieceName(direction).equals(family.baseName()))
                        .orElse(false))
                .findFirst();
    }

    private Optional<Integer> firstFamilyIndexForSlot(WorkspaceDraftSession session, String topologySlotId) {
        return session.familyIndexesForTopologySlot(topologySlotId).stream().findFirst();
    }

    private void applySettings(WorkspaceDraftSession session, HubSpokePlannerSettings settings) {
        session.draft().topologyProfile = settings.applyTo(session.draft().topologyProfile);
    }

    private void removeFamilyAndInactiveSpokeSources(WorkspaceDraftSession session, int removedIndex,
                                                     HubSpokePlannerSettings settings) {
        Set<String> activeSourceSlots = new LinkedHashSet<>(HubSpokePlanner.sourceSpokeSlots(settings));
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new ArrayList<>();
        for (int i = 0; i < session.draft().familyDefinitions.size(); i++) {
            if (i == removedIndex) {
                continue;
            }
            MKWorkspaceRoomFamilyDefinition family = session.draft().familyDefinitions.get(i);
            if (HubSpokePlanner.concreteSpokeSlots().contains(family.topologySlotId()) &&
                    !activeSourceSlots.contains(family.topologySlotId())) {
                continue;
            }
            updated.add(family);
        }
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private MKWorkspaceRoomFamilyDefinition copyWithHeight(MKWorkspaceRoomFamilyDefinition family, int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                Math.max(HubSpokePlanner.MIN_PLATFORM_HEIGHT,
                        Math.min(HubSpokePlanner.MAX_PLATFORM_HEIGHT, height)),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        ).withTemplateCloneSourcePieceName(family.templateCloneSourcePieceName());
    }

    private void ensureSpokeFamiliesForActiveSlots(WorkspaceDraftSession session) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (HubSpokePlanner.usesSharedSpokeSlot(settings)) {
            return;
        }
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new ArrayList<>(session.draft().familyDefinitions);
        settings.spokeTemplates().forEach(template -> HubSpokePlanner.sourceSpokeSlot(template)
                .ifPresent(slot -> ensureSpokeFamily(updated, slot, template)));
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private void ensureSpokeFamily(List<MKWorkspaceRoomFamilyDefinition> families, String topologySlotId,
                                   HubSpokePlannerSettings.SpokeTemplate template) {
        if (families.stream().anyMatch(family -> topologySlotId.equals(family.topologySlotId()) &&
                sourceFamilyBaseName(template).equals(family.baseName()))) {
            return;
        }
        Direction direction = HubSpokePlanner.concreteSpokeDirection(topologySlotId).orElse(Direction.NORTH);
        MKWorkspaceRoomFamilyDefinition shared = families.stream()
                .filter(family -> HubSpokePlanner.SPOKE_SLOT.equals(family.topologySlotId()))
                .findFirst()
                .orElseGet(() -> HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions())
                        .stream()
                        .filter(family -> HubSpokePlanner.SPOKE_SLOT.equals(family.topologySlotId()))
                        .findFirst()
                        .orElseThrow());
        int width = direction.getAxis() == Direction.Axis.X ? template.length() : shared.roomWidth();
        int length = direction.getAxis() == Direction.Axis.X ? shared.roomWidth() : template.length();
        families.add(MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                sourceFamilyBaseName(template),
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, "spoke", "room", false),
                shared.verticalAccessGroupId(),
                shared.supportsVerticalAccess(),
                width,
                length,
                template.height(),
                shared.horizontalExtrusionMode(),
                shared.horizontalExits(),
                shared.topVoidMargin(),
                shared.bottomVoidMargin(),
                shared.foundationPolicyOverride(),
                shared.paletteOverride()
        ));
    }

    private String sourceFamilyBaseName(HubSpokePlannerSettings.SpokeTemplate template) {
        return HubSpokePlanner.sourceSpokeDirection(template)
                .map(template::pieceName)
                .orElse(template.baseName());
    }

    private void ensureCornerFamiliesForActiveSlots(WorkspaceDraftSession session) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new ArrayList<>(session.draft().familyDefinitions);
        if (settings.anySharedCorner()) {
            ensureCornerFamily(updated, HubSpokePlanner.CORNER_SLOT);
        }
        for (String slot : HubSpokePlanner.concreteCornerSlots()) {
            if (settings.uniqueCorner(slot)) {
                ensureCornerFamily(updated, slot);
            }
        }
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private void ensureCornerFamily(List<MKWorkspaceRoomFamilyDefinition> families, String topologySlotId) {
        if (families.stream().anyMatch(family -> topologySlotId.equals(family.topologySlotId()))) {
            return;
        }
        MKWorkspaceRoomFamilyDefinition shared = families.stream()
                .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                .findFirst()
                .orElseGet(() -> HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions())
                        .stream()
                        .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                        .findFirst()
                        .orElseThrow());
        families.add(MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                topologySlotId.replace('.', '_'),
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, "corner", "room", true),
                shared.verticalAccessGroupId(),
                shared.supportsVerticalAccess(),
                shared.roomWidth(),
                shared.roomLength(),
                shared.roomHeight(),
                shared.horizontalExtrusionMode(),
                shared.horizontalExits(),
                shared.topVoidMargin(),
                shared.bottomVoidMargin(),
                shared.foundationPolicyOverride(),
                shared.paletteOverride()
        ));
    }
}
