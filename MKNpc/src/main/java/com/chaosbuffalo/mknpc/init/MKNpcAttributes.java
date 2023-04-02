package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.attributes.NpcAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKNpcAttributes {

    @SubscribeEvent
    public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
        MKCore.LOGGER.info("MKCORE REGISTER ATTRIBUTES");
        event.getRegistry().register(NpcAttributes.AGGRO_RANGE);
    }
}
