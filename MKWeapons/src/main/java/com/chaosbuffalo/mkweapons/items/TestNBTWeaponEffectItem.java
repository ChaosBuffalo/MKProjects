package com.chaosbuffalo.mkweapons.items;

import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.abilities.EmberAbility;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.effects.melee.StunMeleeWeaponEffect;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item.Properties;

public class TestNBTWeaponEffectItem extends Item {

    public TestNBTWeaponEffectItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide() && handIn.equals(InteractionHand.MAIN_HAND)){
            if (playerIn.getOffhandItem().getItem() instanceof IMKMeleeWeapon){
                playerIn.getOffhandItem().getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(cap ->{
                    cap.addMeleeWeaponEffect(new StunMeleeWeaponEffect(0.5, 2));
                    cap.setAbilityId(MKTestAbilities.TEST_EMBER.get().getAbilityId());
                });
                ServerPlayer serverPlayer = (ServerPlayer) playerIn;
                serverPlayer.connection.send(new ClientboundSetEquipmentPacket(playerIn.getId(),
                        Lists.newArrayList(Pair.of(EquipmentSlot.OFFHAND, playerIn.getOffhandItem()))));
                return InteractionResultHolder.success(playerIn.getMainHandItem());
            }
        }
        return InteractionResultHolder.success(playerIn.getMainHandItem());
    }
}
