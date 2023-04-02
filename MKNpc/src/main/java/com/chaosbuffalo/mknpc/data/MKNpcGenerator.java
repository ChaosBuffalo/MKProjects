package com.chaosbuffalo.mknpc.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKNpcGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeServer()){
            gen.addProvider(new NpcDefinitionProvider(gen));
            gen.addProvider(new NpcPoolProvider(gen));
            gen.addProvider(new NpcConfiguredStructureProvider(gen));
            gen.addProvider(new NpcStructureSetProvider(gen));
        }
    }
}
