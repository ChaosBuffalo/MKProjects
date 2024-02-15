package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.item.IMKEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class EntityEquipment {

    private final IMKEntityData entityData;

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

    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        if (from.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityUnequip(entityData.getEntity(), slot, from);
        }
    }

    protected void handleEquip(EquipmentSlot slot, ItemStack to) {
        if (to.getItem() instanceof IMKEquipment equipment) {
            equipment.onEntityEquip(entityData.getEntity(), slot, to);
        }
    }
}
