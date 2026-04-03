package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuestRewardTypes {
    public static final DeferredRegister<QuestRewardType<?>> REGISTRY = DeferredRegister.create(QuestRegistries.QUEST_REWARD_TYPES_REGISTRY_NAME, MKNpc.MODID);


    public static final Supplier<QuestRewardType<XpReward>> XP_REWARD = REGISTRY.register("xp", () -> () -> XpReward.MAP_CODEC);

    public static final Supplier<QuestRewardType<TalentTreeReward>> TALENT_TREE_REWARD = REGISTRY.register("talent_tree", () -> () -> TalentTreeReward.MAP_CODEC);

    public static final Supplier<QuestRewardType<MKLootReward>> MK_LOOT_REWARD = REGISTRY.register("mk_loot", () -> () -> MKLootReward.MAP_CODEC);

    public static final Supplier<QuestRewardType<GrantEntitlementReward>> ENTITLEMENT_REWARD = REGISTRY.register("entitlement", () -> () -> GrantEntitlementReward.MAP_CODEC);

    public static final Supplier<QuestRewardType<FactionReward>> FACTION_REWARD = REGISTRY.register("faction", () -> () -> FactionReward.MAP_CODEC);

    public static final Supplier<QuestRewardType<NotableFactionOverrideReward>> NOTABLE_FACTION_OVERRIDE_REWARD =
            REGISTRY.register("notable_faction_override", () -> () -> NotableFactionOverrideReward.MAP_CODEC);
}
