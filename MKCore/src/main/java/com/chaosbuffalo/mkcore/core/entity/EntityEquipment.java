package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IMKEquipment;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class EntityEquipment {
    public static final ResourceLocation UNARMED_SKILL_ID = MKCore.id("unarmed_skill_mod");
    protected static final float UNARMED_BASE_DAMAGE = 2.0f;

    private final IMKEntityData entityData;

    public EntityEquipment(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public void onEquipmentChange(EquipmentSlot slot, ItemStack from, ItemStack to) {
        // Currently, we only care about swapping items so modifications like durability are ignored
        if (ItemUtils.isEqualNoDurability(from, to))
            return;

        handleRemoval(slot, from);
        handleEquip(slot, to);
    }

    protected void removeUnarmedModifier() {
        AttributeInstance attr = entityData.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(UNARMED_SKILL_ID);
        }
    }

    protected void addUnarmedModifier() {
        AttributeInstance attr = entityData.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            float skillLevel = MKAbility.getSkillLevel(entityData.getEntity(), MKAttributes.HAND_TO_HAND);
            double amount = skillLevel * UNARMED_BASE_DAMAGE * MKConfig.SERVER.skillScalingMultiplier.getAsDouble();
            var modifier = new AttributeModifier(UNARMED_SKILL_ID, amount, AttributeModifier.Operation.ADD_VALUE);
            attr.addOrUpdateTransientModifier(modifier);
        }
    }

    public void refreshUnarmedModifiers(ItemStack mainHand) {
        if (mainHand.isEmpty()) {
            addUnarmedModifier();
        } else {
            removeUnarmedModifier();
        }
    }

    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        if (from.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityUnequip(entityData.getEntity(), slot, from);
        }
    }

    protected void handleEquip(EquipmentSlot slot, ItemStack to) {
        if (to.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityEquip(entityData.getEntity(), slot, to);
        }

        if (slot == EquipmentSlot.MAINHAND) {
            refreshUnarmedModifiers(to);
        }
    }
}
