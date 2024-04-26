package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueEffectTypes;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class AdvanceQuestChainEffect extends DialogueEffect implements IReceivesChainId {
    public static final Codec<AdvanceQuestChainEffect> CODEC = RecordCodecBuilder.<AdvanceQuestChainEffect>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.optionalFieldOf("chainId").forGetter(i -> i.chainId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.chainId))
            ).apply(builder, AdvanceQuestChainEffect::new)
    ).codec();

    private UUID chainId;

    private AdvanceQuestChainEffect(Optional<UUID> chainId) {
        this(chainId.orElse(Util.NIL_UUID));
    }

    public AdvanceQuestChainEffect(UUID chainId) {
        this.chainId = chainId;
    }

    @Override
    public DialogueEffectType<?> getType() {
        return NpcDialogueEffectTypes.ADVANCE_QUEST_CHAIN.get();
    }

    @Override
    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }

    @Override
    public AdvanceQuestChainEffect copy() {
        return new AdvanceQuestChainEffect(chainId);
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity livingEntity, DialogueNode dialogueNode) {
        MKNpc.getPlayerQuestData(player).ifPresent(questLog -> {
            QuestChainInstance questChain = ContentDB.getQuestInstance(chainId);
            if (questChain == null) {
                return;
            }
            questLog.getQuestChain(chainId).ifPresent(playerChain -> {
                IWorldNpcData questDB = ContentDB.getQuestDB();
                for (String questName : playerChain.getCurrentQuests()) {
                    Quest currentQuest = questChain.getDefinition().getQuest(questName);
                    if (currentQuest == null) {
                        continue;
                    }
                    questChain.signalQuestProgress(questDB, questLog, currentQuest, playerChain, true);
                }
            });
        });
    }
}
