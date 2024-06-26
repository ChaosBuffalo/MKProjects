package com.chaosbuffalo.mkultra;


import com.chaosbuffalo.mkultra.extensions.MKUNpcExtensions;
import com.chaosbuffalo.mkultra.init.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKUltra.MODID)
public class MKUltra {
    public static final String MODID = "mkultra";
    public static final Logger LOGGER = LogManager.getLogger();

    public MKUltra() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);
        MKUEffects.register(modBus);
        MKUEntities.register(modBus);
        MKUAbilities.register(modBus);
        MKUWorldGen.register(modBus);
        MKUEntitlements.register(modBus);
        MKUTalents.register(modBus);
        MKUSounds.register(modBus);
        MKUFactions.register(modBus);
        MKUItems.register(modBus);
        modBus.addListener(this::enqueueIMC);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKUItems::registerItemProperties);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        MKUNpcExtensions.sendExtension();
    }
}
