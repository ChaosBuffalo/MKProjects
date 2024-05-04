package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuestObjectiveTypes {
    public static final DeferredRegister<QuestObjectiveType<?>> REGISTRY = DeferredRegister.create(QuestRegistries.QUEST_OBJECTIVE_TYPES_REGISTRY_NAME, MKNpc.MODID);

    public static final Supplier<QuestObjectiveType<KillNotableNpcObjective>> KILL_NOTABLE_NPC = REGISTRY.register("kill_notable_npc", () -> () -> KillNotableNpcObjective.CODEC);

    public static final Supplier<QuestObjectiveType<KillNpcDefObjective>> KILL_NPC_DEF = REGISTRY.register("kill_npc_def", () -> () -> KillNpcDefObjective.CODEC);

    public static final Supplier<QuestObjectiveType<KillWithAbilityObjective>> KILL_WITH_ABILITY = REGISTRY.register("kill_with_ability", () -> () -> KillWithAbilityObjective.CODEC);

    public static final Supplier<QuestObjectiveType<LootChestObjective>> LOOT_CHEST = REGISTRY.register("loot_chest", () -> () -> LootChestObjective.CODEC);

    public static final Supplier<QuestObjectiveType<QuestLootNotableObjective>> QUEST_LOOT_NOTABLE = REGISTRY.register("quest_loot_notable", () -> () -> QuestLootNotableObjective.CODEC);

    public static final Supplier<QuestObjectiveType<QuestLootNpcObjective>> QUEST_LOOT_NPC = REGISTRY.register("quest_loot_npc", () -> () -> QuestLootNpcObjective.CODEC);

    public static final Supplier<QuestObjectiveType<TalkToNpcObjective>> TALK_TO_NPC = REGISTRY.register("talk_to_npc", () -> () -> TalkToNpcObjective.CODEC);

    public static final Supplier<QuestObjectiveType<TradeItemsObjective>> TRADE_WITH_NPC = REGISTRY.register("trade_with_npc", () -> () -> TradeItemsObjective.CODEC);
}
