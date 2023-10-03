package com.chaosbuffalo.mkcore.capabilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CoreCapabilities {

    public static final ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");
    public static final ResourceLocation ENTITY_CAP_ID = MKCore.makeRL("entity_data");

    public static final Capability<MKPlayerData> PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<MKEntityData> ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(MKPlayerData.class);
        event.register(MKEntityData.class);
    }
}
