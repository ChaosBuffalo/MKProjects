package com.chaosbuffalo.mkfaction.faction;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;

public record EntityDefaultFaction(ResourceKey<MKFaction> faction) {
    public static final Codec<EntityDefaultFaction> CODEC = ResourceKey.codec(MKFactionRegistry.FACTION_REGISTRY_KEY)
            .xmap(EntityDefaultFaction::new, EntityDefaultFaction::faction);
}
