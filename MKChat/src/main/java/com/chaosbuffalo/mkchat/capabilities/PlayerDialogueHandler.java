package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDialogueHandler implements IPlayerDialogue {
    private final Player player;
    private final Map<UUID, PlayerConversationMemory> npcEntries;

    public PlayerDialogueHandler(Player player) {
        this.player = player;
        this.npcEntries = new HashMap<>();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public PlayerConversationMemory getConversationMemory(UUID uuid) {
        return npcEntries.computeIfAbsent(uuid, PlayerConversationMemory::new);
    }

    @Override
    public void cleanHistory() {
        npcEntries.clear();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag npcEntriesTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerConversationMemory> entry : npcEntries.entrySet()) {
            npcEntriesTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("npcEntries", npcEntriesTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag npcEntriesTag = nbt.getCompound("npcEntries");
        npcEntries.clear();
        for (String key : npcEntriesTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            PlayerConversationMemory dialogueEntry = new PlayerConversationMemory(uuid);
            dialogueEntry.deserializeNBT(npcEntriesTag.getCompound(key));
            npcEntries.put(uuid, dialogueEntry);
        }
    }
}
