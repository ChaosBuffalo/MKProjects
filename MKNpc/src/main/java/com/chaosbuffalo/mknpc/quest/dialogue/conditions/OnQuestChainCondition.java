package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class OnQuestChainCondition extends DialogueCondition {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "on_quest_chain_condition");
    private UUID questId;

    public OnQuestChainCondition(UUID questId){
        super(conditionTypeName);
        this.questId = questId;
    }

    public OnQuestChainCondition(){
        this(UUID.randomUUID());
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player).map(x -> x.getQuestStatus(questId)
                == PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS).orElse(false);
    }

    @Override
    public OnQuestChainCondition copy() {
        return new OnQuestChainCondition(questId);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.questId = UUID.fromString(dynamic.get("questId").asString(questId.toString()));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("questId"), ops.createString(questId.toString()));
    }
}