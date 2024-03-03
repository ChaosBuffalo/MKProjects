package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.*;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.AdvanceQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.GrantEntitlementEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.InterModComms;

import java.util.UUID;


public class NPCDialogueExtension implements IDialogueExtension {

    public static void sendExtension() {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MKChat.MODID, MKChat.REGISTER_DIALOGUE_EXTENSION, NPCDialogueExtension::new);
    }

    private static Component notable(String name, DialogueTree tree) {
        NotableNpcEntry entry = ContentDB.getPrimaryData().getNotableNpc(UUID.fromString(name));
        if (entry != null) {
            return entry.getName();
        } else {
            return null;
        }
    }

    @Override
    public void registerDialogueExtension() {
        MKNpc.LOGGER.info("Registering MKNpc Dialogue Extension");
        ChatRegistries.putEffectDeserializer(OpenLearnAbilitiesEffect.effectTypeName, OpenLearnAbilitiesEffect::new);
        ChatRegistries.putEffectDeserializer(AdvanceQuestChainEffect.effectTypeName, AdvanceQuestChainEffect::new);
        ChatRegistries.putEffectDeserializer(StartQuestChainEffect.effectTypeName, StartQuestChainEffect::new);
        ChatRegistries.putEffectDeserializer(ObjectiveCompleteEffect.effectTypeName, ObjectiveCompleteEffect::new);
        ChatRegistries.putEffectDeserializer(GrantEntitlementEffect.effectTypeName, GrantEntitlementEffect::new);

        ChatRegistries.putConditionDeserializer(OnQuestCondition.conditionTypeName, OnQuestCondition::new);
        ChatRegistries.putConditionDeserializer(OnQuestChainCondition.conditionTypeName, OnQuestChainCondition::new);
        ChatRegistries.putConditionDeserializer(PendingGenerationCondition.conditionTypeName, PendingGenerationCondition::new);
        ChatRegistries.putConditionDeserializer(HasGeneratedQuestsCondition.conditionTypeName, HasGeneratedQuestsCondition::new);
        ChatRegistries.putConditionDeserializer(HasWeaponInHandCondition.conditionTypeName, HasWeaponInHandCondition::new);
        ChatRegistries.putConditionDeserializer(HasSpentTalentPointsCondition.conditionTypeName, HasSpentTalentPointsCondition::new);
        ChatRegistries.putConditionDeserializer(HasTrainedAbilitiesCondition.conditionTypeName, HasTrainedAbilitiesCondition::new);
        ChatRegistries.putConditionDeserializer(ObjectivesCompleteCondition.conditionTypeName, ObjectivesCompleteCondition::new);
        ChatRegistries.putConditionDeserializer(HasEntitlementCondition.conditionTypeName, HasEntitlementCondition::new);
        ChatRegistries.putConditionDeserializer(CanStartQuestCondition.conditionTypeName, CanStartQuestCondition::new);

        ChatRegistries.putTextComponentProvider("notable", NPCDialogueExtension::notable);
    }
}
