package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FactionOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction");
    public static final Codec<FactionOption> CODEC = ResourceLocation.CODEC.xmap(FactionOption::new, FactionOption::getValue);

    private final ResourceLocation factionId;

    public FactionOption(ResourceLocation factionId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.factionId = factionId;
    }

    public ResourceLocation getValue() {
        return factionId;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        entity.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .ifPresent(cap -> cap.setFactionName(factionId));
    }
}
