package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
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
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.List;

@EventBusSubscriber(modid = MKWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
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
            // This is only for tooltip display
            case IMKAccessory accessory -> addModifierEffects(event, accessory.getAccessoryEffects(stack));
            default -> {
            }
        }
    }

    // This event is needed to apply the attribute values from the curio, but due to how we use the effects for
    // attributes we also need ItemAttributeModifierEvent in order to display the tooltip
    @SubscribeEvent
    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IMKAccessory accessory) {
            addCurioModifierEffects(event, accessory.getAccessoryEffects(stack));
        }
    }

    private static void addCurioModifierEffects(CurioAttributeModifierEvent event, List<? extends IItemEffect> effects) {
        if (effects.isEmpty())
            return;
        for (var effect : effects) {
            if (effect instanceof ItemModifierEffect modifierEffect) {
                for (AttributeOptionEntry m : modifierEffect.getModifiers()) {
                    event.addModifier(m.getAttribute(), m.getModifier());
                }
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
        Entity trueSource = source.getEntity();
        float newDamage = event.getNewDamage();
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

            event.setNewDamage(newDamage);
            MKAccessories.iterateAccessories(event.getEntity(), (accStack, accessory) -> {
                for (IAccessoryEffect effect : accessory.getAccessoryEffects(accStack)) {
                    var nextDamage = effect.modifyDamageDealt(event.getNewDamage(), accessory, accStack, livingTarget, livingSource);
                    event.setNewDamage(nextDamage);
                }
            });

            if (isMelee) {
                ItemStack mainHand = livingSource.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                    for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                        effect.onHurt(newDamage, meleeWeapon, mainHand, livingTarget, livingSource);
                    }
                }
            }
        }
    }
}
