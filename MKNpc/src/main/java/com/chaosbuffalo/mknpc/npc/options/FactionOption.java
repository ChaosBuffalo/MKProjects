package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class FactionOption extends ResourceLocationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction");

    public FactionOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, ResourceLocation value) {
        entity.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .ifPresent(cap -> cap.setFactionName(value));
    }
}
