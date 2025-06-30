package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.accessories.IMKAccessory;
import com.chaosbuffalo.mkweapons.items.armor.MKArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = MKWeapons.MODID, value = Dist.CLIENT)
public class MKWeaponsClientEventHandler {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        switch (item) {
            case MKMeleeWeapon meleeWeapon ->
                    meleeWeapon.addToTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
            case MKBow bow -> bow.addToTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
            case MKArmorItem armorItem ->
                    armorItem.addToTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
            case IMKAccessory accessory ->
                    accessory.addToTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
            default -> {
            }
        }
    }
}
