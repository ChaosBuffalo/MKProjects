package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.init.ChatAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Optional;

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

    static Optional<INpcDialogue> get(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return get(livingEntity);
        }
        return Optional.empty();
    }

    static Optional<INpcDialogue> get(LivingEntity entity) {
        if (entity instanceof Player) {
            return Optional.empty();
        }
        return Optional.of(entity.getData(ChatAttachments.ENTITY_DATA_ATTACHMENT));
    }

    static INpcDialogue getOrThrow(Mob entity) {
        return entity.getData(ChatAttachments.ENTITY_DATA_ATTACHMENT);
    }
}
