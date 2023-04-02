package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.armor.MKArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MKWeaponsClientEventHandler {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (item instanceof MKMeleeWeapon) {
            ((MKMeleeWeapon) item).addToTooltip(event.getItemStack(),
                    event.getPlayer() != null ? event.getPlayer().getCommandSenderWorld() : null, event.getToolTip());
        }
        if (item instanceof MKBow) {
            ((MKBow) item).addToTooltip(event.getItemStack(),
                    event.getPlayer() != null ? event.getPlayer().getCommandSenderWorld() : null, event.getToolTip());
        }
        if (item instanceof MKArmorItem) {
            ((MKArmorItem) item).addToTooltip(event.getItemStack(),
                    event.getPlayer() != null ? event.getPlayer().getCommandSenderWorld() : null, event.getToolTip());
        }

        if (item instanceof MKAccessory){
            ((MKAccessory) item).addToTooltip(event.getItemStack(),
                    event.getPlayer() != null ? event.getPlayer().getCommandSenderWorld() : null, event.getToolTip());
        }

    }

}
