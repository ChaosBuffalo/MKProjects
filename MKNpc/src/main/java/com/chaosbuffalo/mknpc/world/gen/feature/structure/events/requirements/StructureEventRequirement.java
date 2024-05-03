package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class StructureEventRequirement {
    public static final Codec<StructureEventRequirement> CODEC = StructureEventManager.REQ_CODEC;

    private final ResourceLocation requirementType;

    public StructureEventRequirement(ResourceLocation typeName) {
        this.requirementType = typeName;
    }

    public ResourceLocation getTypeName() {
        return requirementType;
    }

    public abstract boolean meetsRequirements(MKStructureEntry entry,
                                              WorldStructureManager.ActiveStructure activeStructure, Level world);

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }
}