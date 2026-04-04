package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.IMKAccessory;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessories;
import com.chaosbuffalo.mkweapons.items.armor.IMKArmor;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.List;

@EventBusSubscriber(modid = MKWeapons.MODID)
public class MKWeaponsEventHandler {

    private static void handleProjectileDamage(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                               IMKEntityData attackerData) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow && !livingTarget.isBlocking()) {
            ItemStack weapon = arrow.getWeaponItem();
            if (weapon != null && weapon.getItem() instanceof IMKRangedWeapon bow) {
                for (IRangedWeaponEffect effect : bow.getWeaponEffects(weapon)) {
                    effect.onProjectileHit(event, source, livingTarget, attackerData,
                            arrow, weapon);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifierEvent(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        switch (stack.getItem()) {
            case IMKMeleeWeapon meleeWeapon -> addModifierEffects(event, meleeWeapon.getWeaponEffects(stack));
            case IMKRangedWeapon rangedWeapon -> addModifierEffects(event, rangedWeapon.getWeaponEffects(stack));
            case IMKArmor armor -> addModifierEffects(event, armor.getArmorEffects(stack));
            // This is only for tooltip display for curios
            case IMKAccessory accessory -> {
                if (accessory.needsAttributesEventSupport()) {
                    addModifierEffects(event, accessory.getAccessoryEffects(stack));
                }
            }
            default -> {
            }
        }
    }

    private static void addModifierEffects(ItemAttributeModifierEvent event, List<? extends IItemEffect> effects) {
        if (effects.isEmpty())
            return;
        for (var effect : effects) {
            if (effect instanceof ItemModifierEffect modifierEffect) {
                for (AttributeOptionEntry m : modifierEffect.getModifiers()) {
                    event.addModifier(m.getAttribute(), m.getModifier(), m.getSlotGroup());
                }
            }
        }
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
        MKAccessories.iterateAccessories(event.getEntity(), (accStack, accessory) -> {
            for (IAccessoryEffect effect : accessory.getAccessoryEffects(accStack)) {
                effect.livingCompleteAbility(event.getEntityData(), accessory, accStack, event.getAbility());
            }
        });
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level().isClientSide)
            return;
        DamageSource source = event.getSource();
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        Entity trueSource = source.getEntity();
        float newDamage = event.getNewDamage();
        boolean isMelee = DamageUtils.isMeleeDamage(source);
        if (trueSource instanceof LivingEntity livingSource) {
            IMKEntityData attackerData = MKCore.getEntityDataOrThrow(livingSource);
            if (DamageUtils.isProjectileDamage(source)) {
                handleProjectileDamage(event, source, livingTarget, attackerData);
            }
            if (isMelee) {
                ItemStack mainHand = livingSource.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                    for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                        newDamage = effect.modifyDamageDealt(newDamage, meleeWeapon,
                                mainHand, livingTarget, livingSource);
                    }
                }
            }

            event.setNewDamage(newDamage);
            MKAccessories.iterateAccessories(event.getEntity(), (accStack, accessory) -> {
                for (IAccessoryEffect effect : accessory.getAccessoryEffects(accStack)) {
                    var nextDamage = effect.modifyDamageDealt(event.getNewDamage(), accessory, accStack, livingTarget, livingSource);
                    event.setNewDamage(nextDamage);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level().isClientSide) {
            return;
        }
        if (event.getNewDamage() <= 0.0f) {
            return;
        }

        DamageSource source = event.getSource();
        if (!DamageUtils.isMeleeDamage(source)) {
            return;
        }

        Entity trueSource = source.getEntity();
        if (trueSource instanceof LivingEntity livingSource) {
            ItemStack mainHand = livingSource.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                    effect.onHurt(event.getNewDamage(), meleeWeapon, mainHand, livingTarget, livingSource);
                }
            }
        }
    }
}
