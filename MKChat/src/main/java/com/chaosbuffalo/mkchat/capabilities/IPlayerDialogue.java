package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.init.ChatAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.UUID;

public interface IPlayerDialogue extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    PlayerConversationMemory getConversationMemory(UUID uuid);

    default PlayerConversationMemory getConversationMemory(LivingEntity target) {
        return getConversationMemory(target.getUUID());
    }

    void cleanHistory();

    static Optional<IPlayerDialogue> get(Player player) {
        return Optional.of(player.getData(ChatAttachments.PLAYER_DATA_ATTACHMENT));
    }
}
