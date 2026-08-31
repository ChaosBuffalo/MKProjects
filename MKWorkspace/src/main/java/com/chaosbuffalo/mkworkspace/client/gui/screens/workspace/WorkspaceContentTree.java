package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentCandidateResolver;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client view model for the topology slot -> content family -> template hierarchy. */
public record WorkspaceContentTree(List<SlotNode> slots) {
    public WorkspaceContentTree {
        slots = List.copyOf(slots);
    }

    public static WorkspaceContentTree build(List<MKWorkspacePieceDefinition> pieces,
                                             Map<String, String> declaredSlotLabels) {
        return build(pieces, declaredSlotLabels, true);
    }

    /** Builds the user-facing tree without allowing stale or inactive piece metadata to declare new slots. */
    public static WorkspaceContentTree buildDeclared(List<MKWorkspacePieceDefinition> pieces,
                                                     Map<String, String> declaredSlotLabels) {
        return build(pieces, declaredSlotLabels, false);
    }

    private static WorkspaceContentTree build(List<MKWorkspacePieceDefinition> pieces,
                                              Map<String, String> declaredSlotLabels,
                                              boolean includeUndeclaredSlots) {
        LinkedHashMap<String, SlotBuilder> slots = new LinkedHashMap<>();
        declaredSlotLabels.forEach((id, label) -> slots.putIfAbsent(id, new SlotBuilder(id, label)));
        for (MKWorkspacePieceDefinition piece : pieces) {
            String slotId = MKWorkspaceContentSelectionTags.topologySlotId(piece);
            if (slotId.isBlank()) continue;
            SlotBuilder slot = slots.get(slotId);
            if (slot == null && includeUndeclaredSlots) {
                slot = new SlotBuilder(slotId, WorkspacePieceDisplay.formatTopologyLabel(slotId));
                slots.put(slotId, slot);
            }
            if (slot == null) continue;
            MKWorkspaceTemplatePurpose purpose = MKWorkspaceContentSelectionTags.purpose(piece);
            if (purpose == MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD) {
                slot.scaffolds.add(piece);
            } else if (purpose.placeable()) {
                String familyId = MKWorkspaceContentSelectionTags.familyId(piece);
                if (!familyId.isBlank()) {
                    slot.families.computeIfAbsent(familyId, FamilyBuilder::new).pieces.add(piece);
                }
            }
        }

        MKWorkspaceContentCandidateResolver.Resolution resolution =
                MKWorkspaceContentCandidateResolver.resolveWorkspacePieces(pieces);
        List<SlotNode> nodes = slots.values().stream()
                .map(slot -> slot.build(resolution))
                .sorted(Comparator.comparing(SlotNode::label).thenComparing(SlotNode::slotId))
                .toList();
        return new WorkspaceContentTree(nodes);
    }

    public FamilyNode family(String slotId, String familyId) {
        if (slotId == null || familyId == null) return null;
        return slots.stream().filter(slot -> slot.slotId().equals(slotId))
                .flatMap(slot -> slot.families().stream())
                .filter(family -> family.familyId().equals(familyId))
                .findFirst().orElse(null);
    }

    public FamilyNode firstFamily() {
        return slots.stream().flatMap(slot -> slot.families().stream()).findFirst().orElse(null);
    }

    public record SlotNode(String slotId, String label, List<MKWorkspacePieceDefinition> scaffolds,
                           List<FamilyNode> families) {
        public SlotNode {
            scaffolds = List.copyOf(scaffolds);
            families = List.copyOf(families);
        }
    }

    public record FamilyNode(String slotId, String familyId, int weight, boolean enabled,
                             boolean canonicalFallback, MKWorkspacePieceDefinition canonical,
                             List<MKWorkspacePieceDefinition> variants, List<MKWorkspacePieceDefinition> pieces,
                             String groupKey) {
        public FamilyNode {
            variants = List.copyOf(variants);
            pieces = List.copyOf(pieces);
        }

        public String status() {
            if (!enabled) return "disabled";
            if (canonicalFallback) return "canonical fallback";
            if (!variants.isEmpty()) return variants.size() + (variants.size() == 1 ? " variant" : " variants");
            return canonical == null ? "missing canonical" : "canonical fallback";
        }
    }

    private static final class SlotBuilder {
        private final String id;
        private final String label;
        private final List<MKWorkspacePieceDefinition> scaffolds = new ArrayList<>();
        private final LinkedHashMap<String, FamilyBuilder> families = new LinkedHashMap<>();

        private SlotBuilder(String id, String label) {
            this.id = id;
            this.label = label == null || label.isBlank() ? WorkspacePieceDisplay.formatTopologyLabel(id) : label;
        }

        private SlotNode build(MKWorkspaceContentCandidateResolver.Resolution resolution) {
            List<FamilyNode> familyNodes = families.values().stream()
                    .map(family -> family.build(id, resolution))
                    .sorted(Comparator.comparing(FamilyNode::familyId))
                    .toList();
            return new SlotNode(id, label, scaffolds.stream()
                    .sorted(Comparator.comparing(MKWorkspacePieceDefinition::pieceName)).toList(), familyNodes);
        }
    }

    private static final class FamilyBuilder {
        private final String id;
        private final List<MKWorkspacePieceDefinition> pieces = new ArrayList<>();

        private FamilyBuilder(String id) {
            this.id = id;
        }

        private FamilyNode build(String slotId, MKWorkspaceContentCandidateResolver.Resolution resolution) {
            List<MKWorkspacePieceDefinition> sorted = pieces.stream()
                    .sorted(Comparator.comparingInt((MKWorkspacePieceDefinition piece) ->
                                    MKWorkspaceContentSelectionTags.purpose(piece).canonical() ? 0 : 1)
                            .thenComparing(MKWorkspacePieceDefinition::pieceName))
                    .toList();
            MKWorkspacePieceDefinition canonical = sorted.stream().filter(piece ->
                    MKWorkspaceContentSelectionTags.purpose(piece).canonical()).findFirst().orElse(null);
            List<MKWorkspacePieceDefinition> variants = sorted.stream().filter(piece ->
                    MKWorkspaceContentSelectionTags.purpose(piece).variant()).toList();
            MKWorkspacePieceDefinition metadata = canonical == null ? sorted.getFirst() : canonical;
            MKWorkspaceContentCandidateResolver.ResolvedFamily resolved = resolution.family(id);
            boolean fallback = resolved != null && resolved.topologySlotId().equals(slotId) &&
                    resolved.canonicalFallback();
            return new FamilyNode(slotId, id, MKWorkspaceContentSelectionTags.familyWeight(metadata.tags()),
                    MKWorkspaceContentSelectionTags.familyEnabled(metadata.tags()), fallback, canonical, variants,
                    sorted, WorkspacePieceDisplay.buildWorkspaceGroupKey(metadata));
        }
    }
}
