package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKWeaponsModClientEventHandler {

    @SubscribeEvent
    public static void onStitchTexture(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
            event.addSprite(new ResourceLocation(MKWeapons.MODID, "item/empty_earring_slot"));
        }
    }
}
