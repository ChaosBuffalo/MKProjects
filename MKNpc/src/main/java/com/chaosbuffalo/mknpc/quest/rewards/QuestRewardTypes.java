package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuestRewardTypes {
    public static final DeferredRegister<QuestRewardType<?>> REGISTRY = DeferredRegister.create(QuestRegistries.QUEST_REWARD_TYPES_REGISTRY_NAME, MKNpc.MODID);


    public static final Supplier<QuestRewardType<XpReward>> XP_REWARD = REGISTRY.register("xp", () -> () -> XpReward.CODEC);

    public static final Supplier<QuestRewardType<TalentTreeReward>> TALENT_TREE_REWARD = REGISTRY.register("talent_tree", () -> () -> TalentTreeReward.CODEC);

    public static final Supplier<QuestRewardType<MKLootReward>> MK_LOOT_REWARD = REGISTRY.register("mk_loot", () -> () -> MKLootReward.CODEC);

    public static final Supplier<QuestRewardType<GrantEntitlementReward>> ENTITLEMENT_REWARD = REGISTRY.register("entitlement", () -> () -> GrantEntitlementReward.CODEC);
}
