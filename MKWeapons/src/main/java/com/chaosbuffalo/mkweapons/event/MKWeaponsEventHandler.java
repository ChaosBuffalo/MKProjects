package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemHandler;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.armor.IMKArmor;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
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
                                               LivingEntity livingSource, IMKEntityData sourceData) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow && !livingTarget.isBlocking()) {
            MKWeapons.getArrowCapability(arrow).ifPresent(cap -> {
                if (!cap.getShootingWeapon().isEmpty() && cap.getShootingWeapon().getItem() instanceof IMKRangedWeapon bow) {
                    for (IRangedWeaponEffect effect : bow.getWeaponEffects(cap.getShootingWeapon())) {
                        effect.onProjectileHit(event, source, livingTarget, sourceData,
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
        Item from = event.getFrom().getItem();
        Item to = event.getTo().getItem();
        if (event.getSlot() == EquipmentSlot.MAINHAND) {
            if (from instanceof IMKWeapon weapon) {
                weapon.getWeaponEffects(event.getFrom()).forEach(eff -> eff.onEntityUnequip(event.getEntity()));
            }
            if (to instanceof IMKWeapon weapon) {
                weapon.getWeaponEffects(event.getTo()).forEach(eff -> eff.onEntityEquip(event.getEntity()));
            }
        }
        if (event.getSlot().isArmor()) {
            if (from instanceof IMKArmor armor) {
                armor.getArmorEffects(event.getFrom()).forEach(eff -> eff.onEntityUnequip(event.getEntity()));
            }
            if (to instanceof IMKArmor armor) {
                armor.getArmorEffects(event.getTo()).forEach(eff -> eff.onEntityEquip(event.getEntity()));
            }
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            checkShieldRestriction(player);
        }
    }

    private static void checkShieldRestriction(ServerPlayer player) {
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (main.getItem() instanceof IMKMeleeWeapon weapon) {
            if (weapon.getWeaponType().isTwoHanded() && offhand.getItem() instanceof ShieldItem) {
                ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
                if (!player.getInventory().add(off)) {
                    player.drop(off, true);
                }
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onPostCombatEvent(PostAttackEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon meleeWeapon) {
            for (IMeleeWeaponEffect effect : meleeWeapon.getWeaponEffects(mainHand)) {
                effect.postAttack(meleeWeapon, mainHand, entity);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingCast(EntityAbilityEvent.EntityCompleteAbilityEvent event) {
        List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(event.getEntity());
        for (MKCurioItemHandler handler : curios) {
            for (IAccessoryEffect effect : handler.getEffects()) {
                effect.livingCompleteAbility(event.getEntity(), event.getEntityData(), handler.getAccessory(),
                        handler.getStack(), event.getAbility());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level.isClientSide)
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
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon) {
                    Item item = mainHand.getItem();
                    for (IMeleeWeaponEffect effect : ((IMKMeleeWeapon) item).getWeaponEffects(mainHand)) {
                        effect.onHurt(newDamage, (IMKMeleeWeapon) item, mainHand, livingTarget, livingSource);
                    }
                }
            }
        }
        event.setAmount(newDamage);
    }
}
