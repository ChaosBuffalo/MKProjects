package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MKCore.MOD_ID, dist = Dist.CLIENT)
public class MKCoreClient {

    public MKCoreClient(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(modContainer, parent));
        modBus.addListener(this::registerLayers);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerReloadListeners);
    }

    public void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(MKCore.id("mk"), MKOverlay.INSTANCE);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PlayerPageRegistry.init();
        event.enqueueWork(CoreItems::registerItemProperties);
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new MeleeAnimationManager.PoseReloadListener());
        event.registerReloadListener(new MeleeAnimationManager.ProfileReloadListener());
        event.registerReloadListener(new SpellAnimationManager.ProfileReloadListener());
    }
}
