package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class AdvanceQuestChainEffect extends DialogueEffect implements IReceivesChainId {
    public static final ResourceLocation effectTypeName = new ResourceLocation(MKNpc.MODID, "advance_quest_chain");
    private UUID chainId;

    public AdvanceQuestChainEffect(UUID chainId) {
        this();
        this.chainId = chainId;
    }

    public AdvanceQuestChainEffect() {
        super(effectTypeName);
        chainId = Util.NIL_UUID;
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
    public void applyEffect(ServerPlayer serverPlayerEntity, LivingEntity livingEntity, DialogueNode dialogueNode) {
        MKNpc.getPlayerQuestData(serverPlayerEntity).ifPresent(questLog -> {
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

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        chainId = dynamic.get("chainId").asString().result().map(UUID::fromString).orElse(Util.NIL_UUID);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (!chainId.equals(Util.NIL_UUID)) {
            builder.put(ops.createString("chainId"), ops.createString(chainId.toString()));
        }
    }
}
