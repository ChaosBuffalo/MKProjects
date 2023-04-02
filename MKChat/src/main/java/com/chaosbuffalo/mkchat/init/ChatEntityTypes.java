package com.chaosbuffalo.mkchat.init;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.entity.TestChatReceiverEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChatEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            MKChat.MODID);


    public static final RegistryObject<EntityType<TestChatReceiverEntity>> TEST_CHAT = ENTITY_TYPES.register(
            "test_entity", () ->
                    EntityType.Builder.<TestChatReceiverEntity>of(TestChatReceiverEntity::new, MobCategory.CREATURE)
                            .sized(EntityType.PIG.getWidth(), EntityType.PIG.getHeight())
                            .build(new ResourceLocation(MKChat.MODID, "test_entity").toString())
    );


    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_CHAT.get(), Pig.createAttributes().build());
    }
}
