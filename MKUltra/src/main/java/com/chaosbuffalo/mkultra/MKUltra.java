package com.chaosbuffalo.mkultra;

import com.chaosbuffalo.mkultra.extensions.MKUNpcExtensions;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkultra.item.MKUArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKUltra.MODID)
public class MKUltra {
    public static final String MODID = "mkultra";
    public static final Logger LOGGER = LogManager.getLogger();

    public MKUltra(IEventBus modBus) {
        MKUEffects.register(modBus);
        MKUEntities.register(modBus);
        MKUAbilities.register(modBus);
        MKUWorldGen.register(modBus);
        MKUSounds.register(modBus);
        MKUItems.register(modBus);
        MKUArmorMaterial.register(modBus);
        modBus.addListener(this::enqueueIMC);
    }



    private void enqueueIMC(final InterModEnqueueEvent event) {
        MKUNpcExtensions.sendExtension();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
