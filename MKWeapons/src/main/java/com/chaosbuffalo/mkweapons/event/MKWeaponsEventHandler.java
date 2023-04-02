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
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKWeapon;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MKWeaponsEventHandler {

    private static void handleProjectileDamage(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                               LivingEntity livingSource, IMKEntityData sourceData){
        if (source.getDirectEntity() instanceof AbstractArrow){
            AbstractArrow arrowEntity = (AbstractArrow) source.getDirectEntity();
            MKWeapons.getArrowCapability(arrowEntity).ifPresent(cap -> {
                if (!livingTarget.isBlocking()){
                    if (!cap.getShootingWeapon().isEmpty() && cap.getShootingWeapon().getItem() instanceof IMKRangedWeapon){
                        IMKRangedWeapon bow = (IMKRangedWeapon) cap.getShootingWeapon().getItem();
                        for (IRangedWeaponEffect effect : bow.getWeaponEffects(cap.getShootingWeapon())){
                            effect.onProjectileHit(event, source, livingTarget, livingSource, sourceData,
                                    arrowEntity, cap.getShootingWeapon());
                        }
                    }
                }
            });
        }
    }


    public static void registerCombatTriggers(){
        SpellTriggers.LIVING_HURT_ENTITY.registerProjectile(MKWeaponsEventHandler::handleProjectileDamage);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        Item from = event.getFrom().getItem();
        Item to = event.getTo().getItem();
        if (event.getSlot() == EquipmentSlot.MAINHAND){
            if (from instanceof IMKWeapon){
                ((IMKWeapon) from).getWeaponEffects(event.getFrom()).forEach(
                        eff -> eff.onEntityUnequip(event.getEntityLiving())
                );
            }
            if (to instanceof IMKWeapon){
                ((IMKWeapon) to).getWeaponEffects(event.getTo()).forEach(
                        eff -> eff.onEntityEquip(event.getEntityLiving())
                );
            }
        }
        if (from instanceof IMKArmor){
            ((IMKArmor) from).getArmorEffects(event.getFrom()).forEach(
                    eff -> eff.onEntityUnequip(event.getEntityLiving())
            );
        }
        if (to instanceof IMKArmor){
            ((IMKArmor) to).getArmorEffects(event.getTo()).forEach(
                    eff -> eff.onEntityEquip(event.getEntityLiving())
            );
        }
        if (!(event.getEntityLiving() instanceof ServerPlayer))
            return;
        ServerPlayer player = (ServerPlayer) event.getEntityLiving();
        checkShieldRestriction(player);
    }

    private static void checkShieldRestriction(ServerPlayer player) {
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (main.getItem() instanceof IMKMeleeWeapon){
            IMKMeleeWeapon weapon = (IMKMeleeWeapon) main.getItem();
            if (weapon.getWeaponType().isTwoHanded() && offhand.getItem() instanceof ShieldItem){
                ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
                if (!player.getInventory().add(off)) {
                    player.drop(off, true);
                }
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onPostCombatEvent(PostAttackEvent event){
        LivingEntity entity = event.getEntityLiving();
        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon){
            Item item = mainHand.getItem();
            for (IMeleeWeaponEffect effect : ((IMKMeleeWeapon) item).getWeaponEffects(mainHand)){
                effect.postAttack((IMKMeleeWeapon) item, mainHand, entity);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingCast(EntityAbilityEvent.EntityCompleteAbilityEvent event){
        List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(event.getEntityLiving());
        for (MKCurioItemHandler handler : curios){
            for (IAccessoryEffect effect : handler.getEffects()){
                effect.livingCompleteAbility(event.getEntityLiving(), event.getEntityData(), handler.getAccessory(),
                        handler.getStack(), event.getAbility());
            }
        }
    }


    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntityLiving();
        if (livingTarget.level.isClientSide)
            return;
        DamageSource source = event.getSource();
        Entity trueSource = source.getEntity();
        float newDamage = event.getAmount();
        if (trueSource instanceof LivingEntity){
            LivingEntity livingSource = (LivingEntity)trueSource;
            if (DamageUtils.isMinecraftPhysicalDamage(source)) {
                ItemStack mainHand = livingSource.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.getItem() instanceof IMKMeleeWeapon){
                    Item item = mainHand.getItem();
                    for (IMeleeWeaponEffect effect : ((IMKMeleeWeapon) item).getWeaponEffects(mainHand)){
                        newDamage = effect.modifyDamageDealt(newDamage, (IMKMeleeWeapon) item,
                                mainHand, livingTarget, livingSource);
                    }
                }
            }
            List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(livingSource);
            for (MKCurioItemHandler handler : curios){
                for (IAccessoryEffect effect : handler.getEffects()){
                    newDamage = effect.modifyDamageDealt(newDamage, handler.getAccessory(),
                            handler.getStack(), livingTarget, livingSource);
                }
            }
        }
        event.setAmount(newDamage);
    }
}
