package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.*;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NpcDialogueConditionTypes {
    public static final DeferredRegister<DialogueConditionType<?>> REGISTRY = DeferredRegister.create(ChatRegistries.CONDITION_TYPES_REGISTRY_NAME, MKNpc.MODID);

    public static final Supplier<DialogueConditionType<CanStartQuestCondition>> CAN_START_QUEST = REGISTRY.register("can_start_quest", () -> () -> CanStartQuestCondition.CODEC);

    public static final Supplier<DialogueConditionType<HasEntitlementCondition>> HAS_ENTITLEMENT = REGISTRY.register("has_entitlement", () -> () -> HasEntitlementCondition.CODEC);

    public static final Supplier<DialogueConditionType<HasGeneratedQuestsCondition>> HAS_GENERATED_QUESTS = REGISTRY.register("has_generated_quests", () -> () -> HasGeneratedQuestsCondition.CODEC);

    public static final Supplier<DialogueConditionType<HasSpentTalentPointsCondition>> HAS_SPENT_TALENTS = REGISTRY.register("has_spent_talents", () -> () -> HasSpentTalentPointsCondition.CODEC);

    public static final Supplier<DialogueConditionType<HasTrainedAbilitiesCondition>> HAS_TRAINED_ABILITIES = REGISTRY.register("has_trained_abilities", () -> () -> HasTrainedAbilitiesCondition.CODEC);

    public static final Supplier<DialogueConditionType<HasWeaponInHandCondition>> HAS_WEAPON_IN_HAND = REGISTRY.register("has_weapon_in_hand", () -> () -> HasWeaponInHandCondition.CODEC);

    public static final Supplier<DialogueConditionType<ObjectivesCompleteCondition>> OBJECTIVES_COMPLETE = REGISTRY.register("objectives_complete", () -> () -> ObjectivesCompleteCondition.CODEC);

    public static final Supplier<DialogueConditionType<OnQuestChainCondition>> ON_QUEST_CHAIN = REGISTRY.register("on_quest_chain", () -> () -> OnQuestChainCondition.CODEC);

    public static final Supplier<DialogueConditionType<OnQuestCondition>> ON_QUEST = REGISTRY.register("on_quest", () -> () -> OnQuestCondition.CODEC);

    public static final Supplier<DialogueConditionType<PendingGenerationCondition>> PENDING_QUEST_GENERATION = REGISTRY.register("has_pending_quest_generation", () -> () -> PendingGenerationCondition.CODEC);
}
