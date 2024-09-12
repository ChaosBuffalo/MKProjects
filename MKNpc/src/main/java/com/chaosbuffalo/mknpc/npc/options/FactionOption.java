package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FactionOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("faction");
    public static final Codec<FactionOption> CODEC = ResourceLocation.CODEC.xmap(FactionOption::new, FactionOption::getValue);
    public static final MapCodec<FactionOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf("factionId").forGetter(i -> i.factionId)
    ).apply(builder, FactionOption::new));

    private final ResourceLocation factionId;

    public FactionOption(ResourceLocation factionId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.factionId = factionId;
    }

    public FactionOption(ResourceKey<MKFaction> factionId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.factionId = factionId.location();
    }

    public ResourceLocation getValue() {
        return factionId;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        IMobFaction.get(entity)
                .ifPresent(cap -> cap.setFactionName(factionId));
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.FACTION.get();
    }
}
