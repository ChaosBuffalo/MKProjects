package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FactionCapabilities {

    public static ResourceLocation PLAYER_FACTION_CAP_ID = new ResourceLocation(MKFactionMod.MODID,
            "player_faction_data");
    public static ResourceLocation MOB_FACTION_CAP_ID = new ResourceLocation(MKFactionMod.MODID,
            "mob_faction_data");

    public static final Capability<IPlayerFaction> PLAYER_FACTION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    public static final Capability<IMobFaction> MOB_FACTION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerFaction.class);
        event.register(IMobFaction.class);
    }
}
