package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.UUID;

public interface IPlayerDialogue extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    PlayerConversationMemory getConversationMemory(UUID uuid);

    default PlayerConversationMemory getConversationMemory(LivingEntity target) {
        return getConversationMemory(target.getUUID());
    }

    void cleanHistory();

    static LazyOptional<IPlayerDialogue> get(Player player) {
        return player.getCapability(ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY);
    }
}
