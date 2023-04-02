package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface INpcDialogue extends INBTSerializable<CompoundTag> {

    boolean hasDialogue();

    void addAdditionalDialogueTree(DialogueTree tree);

    void receiveMessage(ServerPlayer player, String message);

    void startDialogue(ServerPlayer player);

    @Deprecated
    void startDialogue(ServerPlayer player, boolean suppressHail);

    default void hail(ServerPlayer player) {
        startDialogue(player);
    }

    void setDialogueTree(ResourceLocation treeName);

    LivingEntity getEntity();

    @Nullable
    ResourceLocation getDialogueTreeName();

    static LazyOptional<INpcDialogue> get(LivingEntity entity) {
        return entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY);
    }
}
