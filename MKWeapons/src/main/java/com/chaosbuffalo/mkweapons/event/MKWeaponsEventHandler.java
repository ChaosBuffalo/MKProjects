package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.combat.damage.MKDamageCategory;
import com.chaosbuffalo.mkcore.combat.damage.MKDamageContext;
import com.chaosbuffalo.mkcore.combat.damage.MKDamagePipeline;
import com.chaosbuffalo.mkcore.combat.damage.MKDamageStageOrder;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.List;

@EventBusSubscriber(modid = MKWeapons.MODID)
public class MKWeaponsEventHandler {
    private static final String MELEE_DAMAGE_BEFORE_CORE_KEY = "mkweapons.melee_damage_before_core";
    private static boolean damageStagesRegistered;

    private static void handleProjectileDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event,
                                               DamageSource source, LivingEntity livingTarget,
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

    public static void registerCombatTriggers() {
        SpellTriggers.LIVING_HURT_ENTITY.registerProjectile(MKWeaponsEventHandler::handleProjectileDamage);
    }

    public static synchronized void registerDamageStages() {
        if (damageStagesRegistered) {
            return;
        }
        // Route item damage math through MKCore's ordered pipeline so weapon/accessory
        // modifiers execute deterministically relative to the core damage calculations.
        MKDamagePipeline.register(MKDamageStageOrder.WEAPON_MODIFIERS, MKWeapons.id("melee_weapon_damage"),
                MKWeaponsEventHandler::applyMeleeWeaponDamageModifiers);
        MKDamagePipeline.register(MKDamageStageOrder.ACCESSORY_MODIFIERS, MKWeapons.id("accessory_damage"),
                MKWeaponsEventHandler::applyAccessoryDamageModifiers);
        MKDamagePipeline.register(MKDamageStageOrder.POST_REACTIONS, MKWeapons.id("melee_weapon_on_hurt"),
                MKWeaponsEventHandler::applyMeleeWeaponOnHurt);
        damageStagesRegistered = true;
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

    private static void applyMeleeWeaponDamageModifiers(MKDamageContext context) {
        if (context.isFullyBlocked() || context.getCategory() != MKDamageCategory.MELEE) {
            return;
        }
        LivingEntity livingSource = context.getAttacker();
        if (livingSource == null) {
            return;
        }
        float newDamage = context.getWorkingDamage();
        ItemStack mainHand = livingSource.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
            for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                newDamage = effect.modifyDamageDealt(newDamage, meleeWeapon, mainHand, context.getTarget(), livingSource);
            }
            context.setWorkingDamage(newDamage, "mkweapons:melee_weapon_damage");
            context.putMetadata(MELEE_DAMAGE_BEFORE_CORE_KEY, newDamage);
        }
    }

    private static void applyAccessoryDamageModifiers(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        LivingEntity livingSource = context.getAttacker();
        if (livingSource == null) {
            return;
        }
        MKAccessories.iterateAccessories(context.getTarget(), (accStack, accessory) -> {
            for (IAccessoryEffect effect : accessory.getAccessoryEffects(accStack)) {
                var nextDamage = effect.modifyDamageDealt(context.getWorkingDamage(), accessory, accStack,
                        context.getTarget(), livingSource);
                context.setWorkingDamage(nextDamage, "mkweapons:accessory_damage");
            }
        });
    }

    private static void applyMeleeWeaponOnHurt(MKDamageContext context) {
        if (context.isFullyBlocked() || context.getCategory() != MKDamageCategory.MELEE) {
            return;
        }
        LivingEntity livingSource = context.getAttacker();
        if (livingSource == null) {
            return;
        }
        ItemStack mainHand = livingSource.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
            float damageForEffects = context.getWorkingDamage();
            Float preCoreDamage = context.getMetadata(MELEE_DAMAGE_BEFORE_CORE_KEY, Float.class);
            if (preCoreDamage != null) {
                damageForEffects = preCoreDamage;
            }
            for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                effect.onHurt(damageForEffects, meleeWeapon, mainHand, context.getTarget(), livingSource);
            }
        }
    }
}
