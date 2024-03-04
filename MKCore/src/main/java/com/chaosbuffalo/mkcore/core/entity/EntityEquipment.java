package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IMKEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class EntityEquipment {

    private final IMKEntityData entityData;
    protected static final UUID UNARMED_SKILL_MODIFIER = UUID.fromString("bfd1de0f-440c-4029-bcbd-eb25dd89ee83");

    protected static final float UNARMED_BASE_DAMAGE = 2.0f;


    public EntityEquipment(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public void onEquipmentChange(EquipmentSlot slot, ItemStack from, ItemStack to) {
        // Currently, we only care about swapping items so modifications like durability are ignored
        // FIXME: Find the is same item ignore durability func
        if (ItemStack.isSameItemSameTags(from, to))
            return;

        handleRemoval(slot, from);
        handleEquip(slot, to);
    }

    public void removeUnarmedModifier() {
        AttributeInstance attr = entityData.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(UNARMED_SKILL_MODIFIER);
        }
    }

    public void addUnarmedModifier() {
        AttributeInstance attr = entityData.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            if (attr.getModifier(UNARMED_SKILL_MODIFIER) == null) {
                float skillLevel = MKAbility.getSkillLevel(entityData.getEntity(), MKAttributes.HAND_TO_HAND);
                attr.addTransientModifier(new AttributeModifier(UNARMED_SKILL_MODIFIER, "skill scaling",
                        skillLevel * UNARMED_BASE_DAMAGE, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        if (from.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityUnequip(entityData.getEntity(), slot, from);
        }
        if (from.isEmpty()) {
            removeUnarmedModifier();
        }
    }

    protected void handleEquip(EquipmentSlot slot, ItemStack to) {
        if (to.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityEquip(entityData.getEntity(), slot, to);
        }

        if (to.isEmpty()) {
            addUnarmedModifier();
        }
    }
}
