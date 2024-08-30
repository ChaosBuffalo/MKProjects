package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public abstract class NpcDefinitionOption {
    public static final Codec<NpcDefinitionOption> DIRECT_CODEC =
            NpcRegistries.NPC_OPTION_TYPES.byNameCodec().dispatch(NpcDefinitionOption::getType, NpcOptionType::codec);


    public static final Codec<Map<NpcOptionType<?>, NpcDefinitionOption>> OPTION_MAP_CODEC = Codec.dispatchedMap(
            NpcRegistries.NPC_OPTION_TYPES.byNameCodec(), t -> t.codec().codec()
    );



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
    public abstract NpcOptionType<? extends NpcDefinitionOption> getType();
}
