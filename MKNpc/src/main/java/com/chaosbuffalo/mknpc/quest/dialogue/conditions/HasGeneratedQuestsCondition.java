package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class HasGeneratedQuestsCondition extends DialogueCondition {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "has_generated_quests");

    public HasGeneratedQuestsCondition(){
        super(conditionTypeName);
    }


    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return source.getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY).map(
                IEntityNpcData::hasGeneratedQuest).orElse(false);
    }

    @Override
    public HasGeneratedQuestsCondition copy() {
        return new HasGeneratedQuestsCondition();
    }
}

