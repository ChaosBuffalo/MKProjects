package com.chaosbuffalo.mkchat.init;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.entity.TestChatReceiverEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = MKChat.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ChatEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE,
            MKChat.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TestChatReceiverEntity>> TEST_CHAT = ENTITY_TYPES.register(
            "test_entity", () ->
                    EntityType.Builder.<TestChatReceiverEntity>of(TestChatReceiverEntity::new, MobCategory.CREATURE)
                            .sized(EntityType.PIG.getWidth(), EntityType.PIG.getHeight())
                            .build("test_entity")
    );

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_CHAT.get(), Pig.createAttributes().build());
    }
}
