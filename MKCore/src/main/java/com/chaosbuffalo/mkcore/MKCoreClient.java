package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MKCore.MOD_ID, dist = Dist.CLIENT)
public class MKCoreClient {

    public MKCoreClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(modContainer, parent));
    }

    @SubscribeEvent
    public void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "mk"), MKOverlay.INSTANCE);
    }
}
