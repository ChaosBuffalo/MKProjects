package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ModifyBaseMeleeDamageEvent extends EntityDataEvent {
    private final InteractionHand hand;
    private final ItemStack weaponStack;
    private float damage;

    public ModifyBaseMeleeDamageEvent(IMKEntityData data, InteractionHand hand, ItemStack weaponStack, float damage) {
        super(data);
        this.hand = hand;
        this.weaponStack = weaponStack;
        this.damage = damage;
    }

    public LivingEntity getEntity() {
        return getEntityData().getEntity();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}
