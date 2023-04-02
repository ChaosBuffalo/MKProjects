package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.MKNpc;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;


public class HasSpentTalentPointsCondition extends DialogueCondition {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "has_spent_talents");
    private int talentCount;

    public HasSpentTalentPointsCondition(int talentCount){
        super(conditionTypeName);
        this.talentCount = talentCount;
    }

    public HasSpentTalentPointsCondition(){
        this(0);
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKCore.getPlayer(player).map(x -> {
            int unspent = x.getKnowledge().getTalentKnowledge().getUnspentTalentPoints();
            int total = x.getKnowledge().getTalentKnowledge().getTotalTalentPoints();
            int spent = total - unspent;
            return spent >= talentCount;
        }).orElse(false);
    }

    @Override
    public HasSpentTalentPointsCondition copy() {
        return new HasSpentTalentPointsCondition(talentCount);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("talentCount"), ops.createInt(talentCount));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.talentCount = dynamic.get("talentCount").asInt(0);
    }
}
