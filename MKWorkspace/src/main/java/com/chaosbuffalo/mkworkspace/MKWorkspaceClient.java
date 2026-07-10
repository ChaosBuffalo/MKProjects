package com.chaosbuffalo.mkworkspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreenPacketHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKWorkspace.MODID, dist = Dist.CLIENT)
public class MKWorkspaceClient {
    public MKWorkspaceClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MKWorkspaceScreenPacketHandler.register();
    }
}
