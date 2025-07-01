package com.chaosbuffalo.mkultra;


import com.chaosbuffalo.mkultra.client.MKUItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKUltra.MODID, dist = Dist.CLIENT)
public class MKUltraClient {

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKUItemProperties::registerItemProperties);
    }
}
