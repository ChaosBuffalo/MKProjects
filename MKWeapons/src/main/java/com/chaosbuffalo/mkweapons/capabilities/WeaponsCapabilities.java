package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeaponsCapabilities {

    public static ResourceLocation MK_ARROW_CAP_ID = new ResourceLocation(MKWeapons.MODID, "arrow_data");
    public static ResourceLocation MK_WEAPON_CAP_ID = new ResourceLocation(MKWeapons.MODID, "melee_weapon_data");
    public static ResourceLocation MK_ARMOR_CAP_ID = new ResourceLocation(MKWeapons.MODID, "armor_data");

    public static final Capability<IArrowData> ARROW_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IWeaponData> WEAPON_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IArmorData> ARMOR_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IArmorData.class);
        event.register(IWeaponData.class);
        event.register(IArrowData.class);
    }
}
