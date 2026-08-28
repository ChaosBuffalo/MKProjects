package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceInsertFamilyDefinition(
        String familyId,
        MKWorkspaceInsertFamilyKind kind,
        int width,
        int height,
        int depth,
        Optional<MKWorkspaceInsertAttachmentFace> attachmentFace,
        int faceUOffset,
        int faceVOffset,
        String templateJigsawFinalState,
        String templateCloneSourcePieceName
) {
    public static final String TAG_INSERT_FAMILY_ID = MKInsertFamilyPools.TAG_INSERT_FAMILY_ID;
    public static final String TAG_INSERT_FAMILY_KIND = MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND;

    public static final Codec<MKWorkspaceInsertFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("familyId").forGetter(MKWorkspaceInsertFamilyDefinition::familyId),
            MKWorkspaceInsertFamilyKind.CODEC.optionalFieldOf("kind", MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY)
                    .forGetter(MKWorkspaceInsertFamilyDefinition::kind),
            Codec.INT.fieldOf("width").forGetter(MKWorkspaceInsertFamilyDefinition::width),
            Codec.INT.fieldOf("height").forGetter(MKWorkspaceInsertFamilyDefinition::height),
            Codec.INT.fieldOf("depth").forGetter(MKWorkspaceInsertFamilyDefinition::depth),
            MKWorkspaceInsertAttachmentFace.CODEC.optionalFieldOf("attachmentFace")
                    .forGetter(MKWorkspaceInsertFamilyDefinition::attachmentFace),
            Codec.INT.optionalFieldOf("faceUOffset", 0).forGetter(MKWorkspaceInsertFamilyDefinition::faceUOffset),
            Codec.INT.optionalFieldOf("faceVOffset", 0).forGetter(MKWorkspaceInsertFamilyDefinition::faceVOffset),
            Codec.STRING.optionalFieldOf("templateJigsawFinalState", "minecraft:air")
                    .forGetter(MKWorkspaceInsertFamilyDefinition::templateJigsawFinalState),
            Codec.STRING.optionalFieldOf("templateCloneSourcePieceName", "")
                    .forGetter(MKWorkspaceInsertFamilyDefinition::templateCloneSourcePieceName)
    ).apply(instance, MKWorkspaceInsertFamilyDefinition::new));

    public MKWorkspaceInsertFamilyDefinition(String familyId, MKWorkspaceInsertFamilyKind kind, int width,
                                             int height, int depth) {
        this(familyId, kind, width, height, depth, Optional.empty(), 0, 0, "minecraft:air", "");
    }

    public MKWorkspaceInsertFamilyDefinition(String familyId, MKWorkspaceInsertFamilyKind kind, int width,
                                             int height, int depth,
                                             Optional<MKWorkspaceInsertAttachmentFace> attachmentFace,
                                             int faceUOffset,
                                             int faceVOffset,
                                             String templateJigsawFinalState) {
        this(familyId, kind, width, height, depth, attachmentFace, faceUOffset, faceVOffset,
                templateJigsawFinalState, "");
    }

    public MKWorkspaceInsertFamilyDefinition {
        familyId = familyId == null ? "" : familyId.trim();
        kind = kind == null ? MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY : kind.canonical();
        width = Math.max(1, width);
        height = Math.max(1, height);
        depth = Math.max(1, depth);
        attachmentFace = attachmentFace == null ? Optional.empty() : attachmentFace;
        faceUOffset = Math.max(0, faceUOffset);
        faceVOffset = Math.max(0, faceVOffset);
        templateJigsawFinalState = templateJigsawFinalState == null || templateJigsawFinalState.isBlank() ?
                "minecraft:air" :
                templateJigsawFinalState.trim();
        templateCloneSourcePieceName = templateCloneSourcePieceName == null ? "" :
                templateCloneSourcePieceName.trim();
    }

    public static MKWorkspaceInsertFamilyDefinition floorLinkHallway(String familyId, int width, int height,
                                                                     int depth) {
        return new MKWorkspaceInsertFamilyDefinition(familyId, MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY,
                width, height, depth);
    }

    public static MKWorkspaceInsertFamilyDefinition insertSocket(String familyId, int width, int height,
                                                                 int depth) {
        return new MKWorkspaceInsertFamilyDefinition(familyId, MKWorkspaceInsertFamilyKind.INSERT_SOCKET,
                width, height, depth);
    }

    public MKWorkspaceInsertFamilyDefinition withFamilyId(String familyId) {
        return new MKWorkspaceInsertFamilyDefinition(familyId, kind, width, height, depth, attachmentFace,
                faceUOffset, faceVOffset, templateJigsawFinalState, templateCloneSourcePieceName);
    }

    public MKWorkspaceInsertFamilyDefinition withTemplateCloneSourcePieceName(String sourcePieceName) {
        return new MKWorkspaceInsertFamilyDefinition(familyId, kind, width, height, depth, attachmentFace,
                faceUOffset, faceVOffset, templateJigsawFinalState, sourcePieceName);
    }

    public static ResourceLocation poolId(String namespace, String structureName, String familyId) {
        return MKInsertFamilyPools.poolId(namespace, structureName, familyId);
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<>();
        if (familyId.isBlank()) {
            errors.add("insert family id cannot be blank");
        }
        if (width < 1) {
            errors.add("insert family " + familyId + " width must be at least 1");
        }
        if (height < 1) {
            errors.add("insert family " + familyId + " height must be at least 1");
        }
        if (depth < 1) {
            errors.add("insert family " + familyId + " depth must be at least 1");
        }
        if (width > 0 && (width & 1) == 0) {
            errors.add("insert family " + familyId + " width must be odd");
        }
        if (depth > 0 && (depth & 1) == 0) {
            errors.add("insert family " + familyId + " depth must be odd");
        }
        attachmentFace.ifPresent(face -> errors.addAll(
                MKWorkspaceInsertSocketPlacement.validateExteriorFaceOffset(width, height, depth, face,
                        faceUOffset, faceVOffset).stream()
                        .map(error -> "insert family " + familyId + " " + error)
                        .toList()));
        return errors;
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "insert family definition");
    }

    public static MKWorkspaceInsertFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "insert family definition");
    }
}
