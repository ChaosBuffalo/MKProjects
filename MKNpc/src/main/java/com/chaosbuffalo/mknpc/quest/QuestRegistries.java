package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardType;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

public class QuestRegistries {
    public static final ResourceLocation QUEST_REWARD_TYPES_REGISTRY_NAME = new ResourceLocation(MKNpc.MODID, "quest_reward_types");
    public static IForgeRegistry<QuestRewardType<?>> QUEST_REWARDS = null;


    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<QuestRewardType<?>>()
                .setName(QUEST_REWARD_TYPES_REGISTRY_NAME), r -> QUEST_REWARDS = r);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuestRegistries::createRegistries);
        QuestRewardTypes.REGISTRY.register(modBus);
    }
}
