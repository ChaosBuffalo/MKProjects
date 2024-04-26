package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.dialogue.effects.GrantEntitlementEffect;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.AdvanceQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NpcDialogueEffectTypes {
    public static final DeferredRegister<DialogueEffectType<?>> REGISTRY = DeferredRegister.create(ChatRegistries.EFFECT_TYPES_REGISTRY_NAME, MKNpc.MODID);

    public static final Supplier<DialogueEffectType<StartQuestChainEffect>> START_QUEST_CHAIN = REGISTRY.register("start_quest_chain", () -> () -> StartQuestChainEffect.CODEC);

    public static final Supplier<DialogueEffectType<AdvanceQuestChainEffect>> ADVANCE_QUEST_CHAIN = REGISTRY.register("advance_quest_chain", () -> () -> AdvanceQuestChainEffect.CODEC);

    public static final Supplier<DialogueEffectType<GrantEntitlementEffect>> GRANT_ENTITLEMENT = REGISTRY.register("grant_entitlement", () -> () -> GrantEntitlementEffect.CODEC);

    public static final Supplier<DialogueEffectType<ObjectiveCompleteEffect>> OBJECTIVE_COMPLETE = REGISTRY.register("objective_completion", () -> () -> ObjectiveCompleteEffect.CODEC);

    public static final Supplier<DialogueEffectType<OpenLearnAbilitiesEffect>> OPEN_LEARN_ABILITIES = REGISTRY.register("open_learn_abilities", () -> () -> OpenLearnAbilitiesEffect.CODEC);
}
