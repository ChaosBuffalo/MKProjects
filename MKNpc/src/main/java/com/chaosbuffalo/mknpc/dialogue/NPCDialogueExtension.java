package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.ContextAwareTextComponent;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.*;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.AdvanceQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.GrantEntitlementEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;

import java.util.Collections;
import java.util.UUID;
import java.util.function.BiFunction;


public class NPCDialogueExtension implements IDialogueExtension {

    public static void sendExtension() {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MKChat.MODID, MKChat.REGISTER_DIALOGUE_EXTENSION, NPCDialogueExtension::new);
    }

    private static final BiFunction<String, DialogueTree, Component> notableProvider =
            (name, tree) -> new ContextAwareTextComponent("mkchat.simple_context.msg", (context) -> {
                if (context.getPlayer().getServer() != null){
                    Level overworld = context.getPlayer().getServer().getLevel(Level.OVERWORLD);
                    if (overworld != null){
                        return Collections.singletonList(overworld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
                                .map(x -> {
                                    NotableNpcEntry entry = x.getNotableNpc(UUID.fromString(name));
                                    if (entry != null) {
                                        return entry.getName();
                                    } else {
                                        return new TextComponent(String.format("notable:%s", name));
                                    }
                                }).orElse(new TextComponent(String.format("notable:%s", name))));
                    }
                }
                return Collections.singletonList(new TextComponent(String.format("notable:%s", name)));
            });

    @Override
    public void registerDialogueExtension() {
        MKNpc.LOGGER.info("Registering MKNpc Dialogue Extension");
        DialogueManager.putEffectDeserializer(OpenLearnAbilitiesEffect.effectTypeName, OpenLearnAbilitiesEffect::new);
        DialogueManager.putConditionDeserializer(OnQuestCondition.conditionTypeName, OnQuestCondition::new);
        DialogueManager.putConditionDeserializer(OnQuestChainCondition.conditionTypeName, OnQuestChainCondition::new);
        DialogueManager.putConditionDeserializer(PendingGenerationCondition.conditionTypeName, PendingGenerationCondition::new);
        DialogueManager.putConditionDeserializer(HasGeneratedQuestsCondition.conditionTypeName, HasGeneratedQuestsCondition::new);
        DialogueManager.putEffectDeserializer(AdvanceQuestChainEffect.effectTypeName, AdvanceQuestChainEffect::new);
        DialogueManager.putEffectDeserializer(StartQuestChainEffect.effectTypeName, StartQuestChainEffect::new);
        DialogueManager.putEffectDeserializer(ObjectiveCompleteEffect.effectTypeName, ObjectiveCompleteEffect::new);
        DialogueManager.putConditionDeserializer(HasWeaponInHandCondition.conditionTypeName, HasWeaponInHandCondition::new);
        DialogueManager.putConditionDeserializer(HasSpentTalentPointsCondition.conditionTypeName, HasSpentTalentPointsCondition::new);
        DialogueManager.putEffectDeserializer(GrantEntitlementEffect.effectTypeName, GrantEntitlementEffect::new);
        DialogueManager.putConditionDeserializer(HasTrainedAbilitiesCondition.conditionTypeName, HasTrainedAbilitiesCondition::new);
        DialogueManager.putConditionDeserializer(ObjectivesCompleteCondition.conditionTypeName, ObjectivesCompleteCondition::new);
        DialogueManager.putConditionDeserializer(HasEntitlementCondition.conditionTypeName, HasEntitlementCondition::new);
        DialogueManager.putConditionDeserializer(CanStartQuestCondition.conditionTypeName, CanStartQuestCondition::new);
        DialogueManager.putTextComponentProvider("notable", notableProvider);
    }
}
