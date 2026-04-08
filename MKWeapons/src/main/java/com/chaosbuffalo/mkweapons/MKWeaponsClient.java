package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkweapons.client.MKWeaponsItemProperties;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKWeapons.MODID, dist = Dist.CLIENT)
public class MKWeaponsClient {

    public MKWeaponsClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::clientSetup);
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKWeaponsItemProperties::registerItemProperties);
        event.enqueueWork(() -> MeleeAnimationManager.registerResolver(entity -> {
            if (entity.getMainHandItem().getItem() instanceof IMKMeleeWeapon weapon) {
                return weapon.getWeaponType().getName();
            }
            return null;
        }));
    }
}
