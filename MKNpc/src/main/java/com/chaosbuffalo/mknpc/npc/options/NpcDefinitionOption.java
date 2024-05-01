package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class NpcDefinitionOption {
    public static final Codec<NpcDefinitionOption> CODEC = NpcDefinitionManager.NPC_OPTION_CODEC;

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

}
