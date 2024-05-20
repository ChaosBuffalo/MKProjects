package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemHandler;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MKWeaponsEventHandler {

    private static void handleProjectileDamage(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                               IMKEntityData attackerData) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow && !livingTarget.isBlocking()) {
            MKWeapons.getArrowCapability(arrow).ifPresent(cap -> {
                if (!cap.getShootingWeapon().isEmpty() && cap.getShootingWeapon().getItem() instanceof IMKRangedWeapon bow) {
                    for (IRangedWeaponEffect effect : bow.getWeaponEffects(cap.getShootingWeapon())) {
                        effect.onProjectileHit(event, source, livingTarget, attackerData,
                                arrow, cap.getShootingWeapon());
                    }
                }
            });
        }
    }

    public static void registerCombatTriggers() {
        SpellTriggers.LIVING_HURT_ENTITY.registerProjectile(MKWeaponsEventHandler::handleProjectileDamage);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot().getType() == EquipmentSlot.Type.HAND) {
            if (event.getEntity() instanceof ServerPlayer player) {
                checkShieldRestriction(player);
            }
        }
    }

    private static void checkShieldRestriction(ServerPlayer player) {
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (main.getItem() instanceof IMKMeleeWeapon weapon) {
            ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
            if (weapon.getWeaponType().isTwoHanded() && offhand.getItem() instanceof ShieldItem) {
                if (!player.getInventory().add(offhand)) {
                    player.drop(offhand, true);
                }
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onPostAttackEvent(PostAttackEvent event) {
        IMKEntityData attackerData = event.getEntityData();
        LivingEntity entity = attackerData.getEntity();
        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
            for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                effect.postAttack(meleeWeapon, mainHand, attackerData);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingCast(EntityAbilityEvent.EntityCompleteAbilityEvent event) {
        List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(event.getEntity());
        for (MKCurioItemHandler handler : curios) {
            for (IAccessoryEffect effect : handler.getEffects()) {
                effect.livingCompleteAbility(event.getEntityData(), handler.getAccessory(),
                        handler.getStack(), event.getAbility());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.getLevel().isClientSide())
            return;
        DamageSource source = event.getSource();
        Entity trueSource = source.getEntity();
        float newDamage = event.getAmount();
        boolean isMelee = DamageUtils.isMeleeDamage(source);
        if (trueSource instanceof LivingEntity livingSource) {
            if (isMelee) {
                ItemStack mainHand = livingSource.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                    for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                        newDamage = effect.modifyDamageDealt(newDamage, meleeWeapon,
                                mainHand, livingTarget, livingSource);
                    }
                }
            }
            List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(livingSource);
            for (MKCurioItemHandler handler : curios) {
                for (IAccessoryEffect effect : handler.getEffects()) {
                    newDamage = effect.modifyDamageDealt(newDamage, handler.getAccessory(),
                            handler.getStack(), livingTarget, livingSource);
                }
            }
            if (isMelee) {
                ItemStack mainHand = livingSource.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                    for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                        effect.onHurt(newDamage, meleeWeapon, mainHand, livingTarget, livingSource);
                    }
                }
            }
        }
        event.setAmount(newDamage);
    }
}
