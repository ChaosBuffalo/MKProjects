package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class StructureEventCondition {
    public static final Codec<StructureEventCondition> CODEC = StructureEventManager.COND_CODEC;

    private final ResourceLocation typeName;

    public StructureEventCondition(ResourceLocation typeName) {
        this.typeName = typeName;
    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public abstract boolean meetsCondition(MKStructureEntry entry,
                                           WorldStructureManager.ActiveStructure activeStructure, Level world);

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
