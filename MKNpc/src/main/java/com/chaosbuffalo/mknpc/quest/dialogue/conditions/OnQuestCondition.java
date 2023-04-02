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

import java.util.ArrayList;
import java.util.UUID;

public class OnQuestCondition extends DialogueCondition {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "on_quest_condition");
    private UUID questId;
    private String questStep;

    public OnQuestCondition(UUID questId, String questStep){
        super(conditionTypeName);
        this.questId = questId;
        this.questStep = questStep;
    }

    public OnQuestCondition(){
        this(UUID.randomUUID(), "invalid");
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player).map(
                x -> x.getQuestStatus(questId) == PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS
                        && x.getCurrentQuestSteps(questId).orElse(new ArrayList<>()).contains(questStep))
                .orElse(false);
    }

    @Override
    public OnQuestCondition copy() {
        return new OnQuestCondition(questId, questStep);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("questId"), ops.createString(questId.toString()));
        builder.put(ops.createString("questStep"), ops.createString(questStep));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.questId = UUID.fromString(dynamic.get("questId").asString(questId.toString()));
        this.questStep = dynamic.get("questStep").asString("invalid");
    }

}
