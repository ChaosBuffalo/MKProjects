package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerSettingsEntry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Produces stable, human-readable definition changes without relying on model identity strings. */
final class MKWorkspaceDefinitionFieldDiff {
    private static final String NOT_PRESENT = "not present";

    private MKWorkspaceDefinitionFieldDiff() {
    }

    static List<MKWorkspaceFieldChange> differences(MKStructureWorkspace before, MKStructureWorkspace after) {
        ArrayList<MKWorkspaceFieldChange> fields = new ArrayList<>();
        add(fields, "namespace", before.namespace(), after.namespace());
        add(fields, "structure name", before.structureName(), after.structureName());

        add(fields, "topology planner", before.topologyProfile().plannerId(), after.topologyProfile().plannerId());
        add(fields, "terrain adjustment",
                before.topologyProfile().terrainAdjustment().getSerializedName(),
                after.topologyProfile().terrainAdjustment().getSerializedName());
        addKeyed(fields, "planner setting", before.topologyProfile().plannerSettings(),
                after.topologyProfile().plannerSettings(), MKWorkspaceDefinitionFieldDiff::plannerSettingKey,
                MKWorkspacePlannerSettingsEntry.CODEC, MKWorkspaceDefinitionFieldDiff::describePlannerSetting);

        addDimensions(fields, before, after);
        addPalette(fields, "palette", before.palette(), after.palette());
        addStairs(fields, "stairs", before.stairConfig(), after.stairConfig());
        add(fields, "vertical access placement",
                before.verticalAccessPlacement().getSerializedName(),
                after.verticalAccessPlacement().getSerializedName());
        add(fields, "shell margin", before.shellMargin(), after.shellMargin());
        add(fields, "vertical shell margin", before.verticalShellMargin(), after.verticalShellMargin());
        add(fields, "exterior air margin", before.exteriorAirMargin(), after.exteriorAirMargin());
        add(fields, "preview margin", before.previewMargin(), after.previewMargin());
        addVerticalAccess(fields, before.verticalAccessSpec(), after.verticalAccessSpec());

        addKeyed(fields, "room family", before.familyDefinitions(), after.familyDefinitions(),
                MKWorkspaceRoomFamilyDefinition::baseName, MKWorkspaceRoomFamilyDefinition.CODEC,
                MKWorkspaceDefinitionFieldDiff::describeRoomFamily);
        addKeyed(fields, "opening profile", before.openingProfiles(), after.openingProfiles(),
                MKHorizontalOpeningProfile::profileId, MKHorizontalOpeningProfile.CODEC,
                MKWorkspaceDefinitionFieldDiff::describeOpeningProfile);
        addKeyed(fields, "linear run family", before.linearRunFamilies(), after.linearRunFamilies(),
                MKWorkspaceLinearRunFamilyDefinition::linearRunId, MKWorkspaceLinearRunFamilyDefinition.CODEC,
                MKWorkspaceDefinitionFieldDiff::describeLinearRunFamily);
        addKeyed(fields, "insert family", before.insertFamilies(), after.insertFamilies(),
                MKWorkspaceInsertFamilyDefinition::familyId, MKWorkspaceInsertFamilyDefinition.CODEC,
                MKWorkspaceDefinitionFieldDiff::describeInsertFamily);
        return List.copyOf(fields);
    }

    private static void addDimensions(List<MKWorkspaceFieldChange> fields, MKStructureWorkspace before,
                                      MKStructureWorkspace after) {
        add(fields, "dimensions.room width", before.dimensions().roomWidth(), after.dimensions().roomWidth());
        add(fields, "dimensions.room length", before.dimensions().roomLength(), after.dimensions().roomLength());
        add(fields, "dimensions.entrance height", before.dimensions().entranceHeight(),
                after.dimensions().entranceHeight());
        add(fields, "dimensions.room height", before.dimensions().roomHeight(), after.dimensions().roomHeight());
        add(fields, "dimensions.basement height", before.dimensions().basementHeight(),
                after.dimensions().basementHeight());
        add(fields, "dimensions.shaft width", before.dimensions().shaftWidth(), after.dimensions().shaftWidth());
        add(fields, "dimensions.doorway width", before.dimensions().doorwayWidth(),
                after.dimensions().doorwayWidth());
        add(fields, "dimensions.doorway height", before.dimensions().doorwayHeight(),
                after.dimensions().doorwayHeight());
    }

    private static void addPalette(List<MKWorkspaceFieldChange> fields, String prefix,
                                   MKWorkspaceMaterialPalette before, MKWorkspaceMaterialPalette after) {
        add(fields, prefix + ".floor block", before.floorBlock(), after.floorBlock());
        add(fields, prefix + ".wall block", before.wallBlock(), after.wallBlock());
        add(fields, prefix + ".ceiling block", before.ceilingBlock(), after.ceilingBlock());
        add(fields, prefix + ".stair block", before.stairBlock(), after.stairBlock());
        add(fields, prefix + ".slab block", before.slabBlock(), after.slabBlock());
        add(fields, prefix + ".ladder block", before.ladderBlock(), after.ladderBlock());
    }

    private static void addStairs(List<MKWorkspaceFieldChange> fields, String prefix,
                                  MKWorkspaceStairAuthoringConfig before,
                                  MKWorkspaceStairAuthoringConfig after) {
        add(fields, prefix + ".mode", before.mode().getSerializedName(), after.mode().getSerializedName());
        add(fields, prefix + ".rise type", before.riseType().getSerializedName(),
                after.riseType().getSerializedName());
        add(fields, prefix + ".width", before.stairWidth(), after.stairWidth());
    }

    private static void addVerticalAccess(List<MKWorkspaceFieldChange> fields,
                                          MKWorkspaceVerticalAccessSpec before,
                                          MKWorkspaceVerticalAccessSpec after) {
        add(fields, "vertical access.shaft size", before.shaftSize(), after.shaftSize());
        add(fields, "vertical access.placement", before.placement().getSerializedName(),
                after.placement().getSerializedName());
        addStairs(fields, "vertical access stairs", before.stairConfig(), after.stairConfig());
    }

    private static <T> void addKeyed(List<MKWorkspaceFieldChange> fields, String label,
                                     List<T> before, List<T> after, Function<T, String> keyFunction,
                                     Codec<T> codec, Function<T, String> description) {
        List<String> beforeOrder = before.stream().map(keyFunction).toList();
        List<String> afterOrder = after.stream().map(keyFunction).toList();
        if (!beforeOrder.equals(afterOrder)) {
            add(fields, label + " order", describeOrder(beforeOrder), describeOrder(afterOrder));
        }

        Map<String, T> beforeByKey = indexByKey(before, keyFunction);
        Map<String, T> afterByKey = indexByKey(after, keyFunction);
        LinkedHashSet<String> keys = new LinkedHashSet<>(beforeByKey.keySet());
        keys.addAll(afterByKey.keySet());
        for (String key : keys) {
            T beforeValue = beforeByKey.get(key);
            T afterValue = afterByKey.get(key);
            if (beforeValue == null) {
                fields.add(new MKWorkspaceFieldChange(label + " " + key, NOT_PRESENT,
                        description.apply(afterValue)));
            } else if (afterValue == null) {
                fields.add(new MKWorkspaceFieldChange(label + " " + key, description.apply(beforeValue),
                        NOT_PRESENT));
            } else if (!semanticallyEqual(codec, beforeValue, afterValue)) {
                fields.add(new MKWorkspaceFieldChange(label + " " + key, description.apply(beforeValue),
                        description.apply(afterValue)));
            }
        }
    }

    private static <T> Map<String, T> indexByKey(List<T> values, Function<T, String> keyFunction) {
        LinkedHashMap<String, T> result = new LinkedHashMap<>();
        for (T value : values) {
            result.put(keyFunction.apply(value), value);
        }
        return result;
    }

    private static <T> boolean semanticallyEqual(Codec<T> codec, T before, T after) {
        return codec.encodeStart(JsonOps.INSTANCE, before).getOrThrow()
                .equals(codec.encodeStart(JsonOps.INSTANCE, after).getOrThrow());
    }

    private static void add(List<MKWorkspaceFieldChange> fields, String name, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            fields.add(new MKWorkspaceFieldChange(name, String.valueOf(before), String.valueOf(after)));
        }
    }

    private static String describeOrder(List<String> keys) {
        return keys.isEmpty() ? "none" : String.join(", ", keys);
    }

    private static String plannerSettingKey(MKWorkspacePlannerSettingsEntry entry) {
        return entry.plannerId() + "/" + entry.scopeId();
    }

    private static String describePlannerSetting(MKWorkspacePlannerSettingsEntry entry) {
        return "palette=" + describePaletteOverride(entry.paletteOverride().orElse(null)) +
                "; settings=" + (entry.settings().isEmpty() ? "none" : entry.settings());
    }

    private static String describeRoomFamily(MKWorkspaceRoomFamilyDefinition family) {
        String size = inherited(family.roomWidth()) + "x" + inherited(family.roomLength()) + "x" +
                inherited(family.roomHeight());
        String verticalAccess = family.supportsVerticalAccess() ?
                "yes" + (family.verticalAccessGroupId().isBlank() ? "" : " (" + family.verticalAccessGroupId() + ")") :
                "no";
        return "slot=" + family.topologySlotId() +
                "; role=" + family.slotMetadata().roleKind() + "/" + family.slotMetadata().pieceKind() +
                "; size=" + size +
                "; vertical access=" + verticalAccess +
                "; extrusion=" + family.horizontalExtrusionMode().getSerializedName() +
                "; exits=" + family.horizontalExitSummary().replace('|', ',') +
                "; void margins=top " + family.topVoidMargin() + ", bottom " + family.bottomVoidMargin() +
                "; source slot=" + blankAsDefault(family.sourceTopologySlotId(), "self") +
                "; settings slot=" + blankAsDefault(family.settingsTopologySlotId(), "source") +
                "; clone source=" + blankAsDefault(family.templateCloneSourcePieceName(), "none") +
                "; foundation=" + describeFoundation(family.foundationPolicyOverride()) +
                "; palette=" + describePaletteOverride(family.paletteOverride());
    }

    private static String describeOpeningProfile(MKHorizontalOpeningProfile profile) {
        ArrayList<String> paths = new ArrayList<>();
        if (profile.allowOnMainPath()) {
            paths.add("main");
        }
        if (profile.allowOnBranchPath()) {
            paths.add("branch");
        }
        return "opening=" + profile.openingWidth() + "x" + profile.openingHeight() +
                "; allowed paths=" + (paths.isEmpty() ? "none" : String.join(", ", paths));
    }

    private static String describeLinearRunFamily(MKWorkspaceLinearRunFamilyDefinition family) {
        String shapes = family.supportedShapes().stream()
                .map(shape -> shape.getSerializedName()).collect(Collectors.joining(", "));
        ArrayList<String> paths = new ArrayList<>();
        if (family.allowOnMainPath()) {
            paths.add("main");
        }
        if (family.allowOnBranchPath()) {
            paths.add("branch");
        }
        return "slot=" + blankAsDefault(family.topologySlotId(), "default") +
                "; kind=" + family.kind().getSerializedName() +
                "; opening profile=" + family.openingProfileId() +
                "; length=" + family.length() +
                "; interior=" + family.interiorWidth() + "x" + family.interiorHeight() +
                "; slope delta=" + family.slopeDelta() +
                "; paths=" + (paths.isEmpty() ? "none" : String.join(", ", paths)) +
                "; projection=" + family.projection().getSerializedName() +
                "; shapes=" + (shapes.isBlank() ? "none" : shapes) +
                "; top void margin=" + family.topVoidMargin() +
                "; foundation=" + describeFoundation(family.foundationPolicy()) +
                "; palette=" + describePaletteOverride(family.paletteOverride());
    }

    private static String describeInsertFamily(MKWorkspaceInsertFamilyDefinition family) {
        return "kind=" + family.kind().getSerializedName() +
                "; size=" + family.width() + "x" + family.height() + "x" + family.depth() +
                "; attachment face=" + family.attachmentFace()
                .map(face -> face.getSerializedName()).orElse("automatic") +
                "; face offset=" + family.faceUOffset() + "," + family.faceVOffset() +
                "; final state=" + family.templateJigsawFinalState() +
                "; clone source=" + blankAsDefault(family.templateCloneSourcePieceName(), "none");
    }

    private static String describeFoundation(@Nullable MKWorkspaceFoundationPolicy policy) {
        if (policy == null || !policy.enabled()) {
            return "none";
        }
        return switch (policy.mode()) {
            case UNIFORM_STATE -> policy.mode().getSerializedName() + " (" +
                    policy.foundationBlockOpt().map(Object::toString).orElse("missing block") + ")";
            case MASKED_EXTEND_BOTTOM_BLOCKS -> policy.mode().getSerializedName() + " (mask: " +
                    policy.maskBlocks().stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
            default -> policy.mode().getSerializedName();
        };
    }

    private static String describePaletteOverride(@Nullable MKWorkspacePaletteOverride palette) {
        if (palette == null || palette.isEmpty()) {
            return "workspace default";
        }
        ArrayList<String> values = new ArrayList<>();
        palette.floorBlockOpt().ifPresent(value -> values.add("floor=" + value));
        palette.wallBlockOpt().ifPresent(value -> values.add("wall=" + value));
        palette.ceilingBlockOpt().ifPresent(value -> values.add("ceiling=" + value));
        palette.stairBlockOpt().ifPresent(value -> values.add("stairs=" + value));
        palette.slabBlockOpt().ifPresent(value -> values.add("slab=" + value));
        palette.ladderBlockOpt().ifPresent(value -> values.add("ladder=" + value));
        return String.join(", ", values);
    }

    private static String inherited(int value) {
        return value > 0 ? Integer.toString(value) : "inherited";
    }

    private static String blankAsDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
