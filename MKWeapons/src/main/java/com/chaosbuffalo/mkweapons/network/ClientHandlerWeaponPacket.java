package com.chaosbuffalo.mkweapons.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

public class ClientHandlerWeaponPacket {


    @OnlyIn(Dist.CLIENT)
    public static void handlePacket(SyncWeaponTypesPacket packet){
        if (Minecraft.getInstance().player != null){
            WeaponTypeManager.handleMKWeaponReloadForPlayerPre(Minecraft.getInstance().player);
        }
        for (Map.Entry<ResourceLocation, CompoundTag> meleeWeaponPair : packet.data.entrySet()) {
            IMeleeWeaponType weaponType = MeleeWeaponTypes.getWeaponType(meleeWeaponPair.getKey());
            if (weaponType != null) {
                MKCore.LOGGER.debug("Updating melee weapon type with server data: {}", meleeWeaponPair.getKey());
                weaponType.deserialize(new Dynamic<>(NbtOps.INSTANCE, meleeWeaponPair.getValue()));
            } else {
                MKCore.LOGGER.warn("Skipping melee weapon type update for {}", meleeWeaponPair.getKey());
            }
        }
        WeaponTypeManager.refreshAllWeapons();
        if (Minecraft.getInstance().player != null){
            WeaponTypeManager.handleMKWeaponReloadForPlayerPost(Minecraft.getInstance().player);
        }
    }
}
