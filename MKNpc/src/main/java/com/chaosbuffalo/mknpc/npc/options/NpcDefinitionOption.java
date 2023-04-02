package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public abstract class NpcDefinitionOption implements IDynamicMapTypedSerializer {
    private static final String TYPE_NAME_FIELD = "optionType";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKNpc.MODID, "npc_option.invalid");
    private final ResourceLocation name;

    public enum ApplyOrder {
        EARLY,
        MIDDLE,
        LATE
    }

    private final ApplyOrder ordering;

    public NpcDefinitionOption(ResourceLocation name, ApplyOrder order) {
        this.name = name;
        this.ordering = order;
    }

    public ApplyOrder getOrdering() {
        return ordering;
    }

    public ResourceLocation getName() {
        return name;
    }

    public abstract void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel);

    public boolean canBeBossStage() {
        return false;
    }

    @Override
    public ResourceLocation getTypeName() {
        return name;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {

    }

    public abstract <D> void readAdditionalData(Dynamic<D> dynamic);

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }
}
