package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NpcDialogueConditionTypes {
    public static final DeferredRegister<DialogueConditionType<?>> REGISTRY = DeferredRegister.create(ChatRegistries.CONDITION_TYPES_REGISTRY_NAME, MKNpc.MODID);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<CanStartQuestCondition>> CAN_START_QUEST = REGISTRY.register("can_start_quest", () -> () -> CanStartQuestCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<HasEntitlementCondition>> HAS_ENTITLEMENT = REGISTRY.register("has_entitlement", () -> () -> HasEntitlementCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<HasGeneratedQuestsCondition>> HAS_GENERATED_QUESTS = REGISTRY.register("has_generated_quests", () -> () -> HasGeneratedQuestsCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<HasSpentTalentPointsCondition>> HAS_SPENT_TALENTS = REGISTRY.register("has_spent_talents", () -> () -> HasSpentTalentPointsCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<HasTrainedAbilitiesCondition>> HAS_TRAINED_ABILITIES = REGISTRY.register("has_trained_abilities", () -> () -> HasTrainedAbilitiesCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<HasWeaponInHandCondition>> HAS_WEAPON_IN_HAND = REGISTRY.register("has_weapon_in_hand", () -> () -> HasWeaponInHandCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<ObjectivesCompleteCondition>> OBJECTIVES_COMPLETE = REGISTRY.register("objectives_complete", () -> () -> ObjectivesCompleteCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<OnQuestChainCondition>> ON_QUEST_CHAIN = REGISTRY.register("on_quest_chain", () -> () -> OnQuestChainCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<OnQuestCondition>> ON_QUEST = REGISTRY.register("on_quest", () -> () -> OnQuestCondition.MAP_CODEC);

    public static final DeferredHolder<DialogueConditionType<?>, DialogueConditionType<PendingGenerationCondition>> PENDING_QUEST_GENERATION = REGISTRY.register("has_pending_quest_generation", () -> () -> PendingGenerationCondition.CODEC);
}
