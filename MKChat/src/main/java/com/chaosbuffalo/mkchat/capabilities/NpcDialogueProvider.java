package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;

public class NpcDialogueProvider extends ChatCapabilities.Provider<LivingEntity, INpcDialogue> {

    public NpcDialogueProvider(LivingEntity attached) {
        super(attached);
    }

    @Override
    INpcDialogue makeData(LivingEntity attached) {
        return new NpcDialogueHandler(attached);
    }

    @Override
    Capability<INpcDialogue> getCapability() {
        return ChatCapabilities.NPC_DIALOGUE_CAPABILITY;
    }
}
