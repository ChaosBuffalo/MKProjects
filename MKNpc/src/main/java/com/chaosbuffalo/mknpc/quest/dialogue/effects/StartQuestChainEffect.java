package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.dialogue.effects.NpcDialogueEffectTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class StartQuestChainEffect extends DialogueEffect implements IReceivesChainId {
    public static final Codec<StartQuestChainEffect> CODEC = RecordCodecBuilder.<StartQuestChainEffect>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.optionalFieldOf("chainId").forGetter(i -> i.chainId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.chainId))
            ).apply(builder, StartQuestChainEffect::new)
    ).codec();

    private UUID chainId;


    private StartQuestChainEffect(Optional<UUID> chainId) {
        this(chainId.orElse(Util.NIL_UUID));
    }

    public StartQuestChainEffect(UUID chainId) {
        this.chainId = chainId;
    }

    @Override
    public DialogueEffectType<?> getType() {
        return NpcDialogueEffectTypes.START_QUEST_CHAIN.get();
    }

    @Override
    public StartQuestChainEffect copy() {
        return new StartQuestChainEffect(chainId);
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity livingEntity, DialogueNode dialogueNode) {
        MKNpc.getPlayerQuestData(player).ifPresent(questLog -> {
            questLog.startQuest(ContentDB.getQuestDB(), chainId);
        });
    }

    @Override
    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }
}
