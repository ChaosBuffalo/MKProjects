package com.chaosbuffalo.mkultra;


import com.chaosbuffalo.mkultra.client.MKUItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKUltra.MODID, dist = Dist.CLIENT)
public class MKUltraClient {

    public MKUltraClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::clientSetup);
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKUItemProperties::registerItemProperties);
    }
}
