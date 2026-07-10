package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record MKWorkspaceInsertFamilyDefinition(
        String familyId,
        MKWorkspaceInsertFamilyKind kind,
        int width,
        int height,
        int depth
) {
    public static final String TAG_INSERT_FAMILY_ID = MKInsertFamilyPools.TAG_INSERT_FAMILY_ID;
    public static final String TAG_INSERT_FAMILY_KIND = MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND;

    public static final Codec<MKWorkspaceInsertFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("familyId").forGetter(MKWorkspaceInsertFamilyDefinition::familyId),
            MKWorkspaceInsertFamilyKind.CODEC.optionalFieldOf("kind", MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY)
                    .forGetter(MKWorkspaceInsertFamilyDefinition::kind),
            Codec.INT.fieldOf("width").forGetter(MKWorkspaceInsertFamilyDefinition::width),
            Codec.INT.fieldOf("height").forGetter(MKWorkspaceInsertFamilyDefinition::height),
            Codec.INT.fieldOf("depth").forGetter(MKWorkspaceInsertFamilyDefinition::depth)
    ).apply(instance, MKWorkspaceInsertFamilyDefinition::new));

    public MKWorkspaceInsertFamilyDefinition {
        familyId = familyId == null ? "" : familyId.trim();
        kind = kind == null ? MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY : kind;
        width = Math.max(1, width);
        height = Math.max(1, height);
        depth = Math.max(1, depth);
    }

    public static MKWorkspaceInsertFamilyDefinition floorLinkHallway(String familyId, int width, int height,
                                                                     int depth) {
        return new MKWorkspaceInsertFamilyDefinition(familyId, MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY,
                width, height, depth);
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
        return errors;
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "insert family definition");
    }

    public static MKWorkspaceInsertFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "insert family definition");
    }
}
