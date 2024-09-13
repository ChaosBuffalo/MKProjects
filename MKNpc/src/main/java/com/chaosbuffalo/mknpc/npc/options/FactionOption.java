package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FactionOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("faction");
    public static final MapCodec<FactionOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceKey.codec(MKFactionRegistry.FACTION_REGISTRY_KEY).fieldOf("factionId").forGetter(i -> i.factionId)
    ).apply(builder, FactionOption::new));

    private final ResourceKey<MKFaction> factionId;

    public FactionOption(ResourceKey<MKFaction> factionId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.factionId = factionId;
    }

    public ResourceLocation getValue() {
        return factionId.location();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        IMobFaction.get(entity).ifPresent(cap -> {
            Holder<MKFaction> faction = MKFactionRegistry.getFactionHolder(entity.registryAccess(), factionId).orElseGet(() -> {
                MKNpc.LOGGER.error("Tried to apply invalid faction {} to entity {}", factionId, entity);
                return null;
            });
            cap.setFaction(faction);
        });
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.FACTION.get();
    }
}
