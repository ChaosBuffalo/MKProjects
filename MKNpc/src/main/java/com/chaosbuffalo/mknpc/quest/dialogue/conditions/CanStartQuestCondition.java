package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

import java.util.UUID;

public class CanStartQuestCondition extends DialogueCondition implements IReceivesChainId {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "can_start_chain_condition");
    private UUID questId;
    private boolean allowRepeat;

    public CanStartQuestCondition(UUID questId, boolean allowRepeat){
        super(conditionTypeName);
        this.questId = questId;
        this.allowRepeat = allowRepeat;
    }

    public CanStartQuestCondition(){
        this(Util.NIL_UUID, false);
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player).map(x -> {
            PlayerQuestingDataHandler.QuestStatus status = x.getQuestStatus(questId);
            if (status == PlayerQuestingDataHandler.QuestStatus.NOT_ON){
                return true;
            } else {
                return allowRepeat && status != PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS;
            }
        }).orElse(false);
    }

    @Override
    public CanStartQuestCondition copy() {
        return new CanStartQuestCondition(questId, allowRepeat);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        questId = dynamic.get("questId").asString().result().map(UUID::fromString).orElse(Util.NIL_UUID);
        allowRepeat = dynamic.get("allowRepeat").asBoolean(false);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (!questId.equals(Util.NIL_UUID)){
            builder.put(ops.createString("questId"), ops.createString(questId.toString()));
        }
        builder.put(ops.createString("allowRepeat"), ops.createBoolean(allowRepeat));
    }

    @Override
    public void setChainId(UUID chainId) {
        this.questId = chainId;
    }
}