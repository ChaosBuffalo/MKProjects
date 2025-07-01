package com.chaosbuffalo.mkweapons.items.randomization.slots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;

public class LootSlot {
    public static final Codec<LootSlot> CODEC = ResourceLocation.CODEC.comapFlatMap(slotName -> {
        LootSlot slot = LootSlotManager.getSlotFromName(slotName);
        if (slot != null) {
            return DataResult.success(slot);
        }
        return DataResult.error(() -> "Loot slot " + slotName + " not registered with LootSlotManager");
    }, LootSlot::getName);

    private final ResourceLocation name;
    private final EquipmentSlotGroup slotGroup;

    public LootSlot(ResourceLocation name) {
        this(name, EquipmentSlotGroup.ANY);
    }

    public LootSlot(ResourceLocation name, EquipmentSlotGroup slotGroup) {
        this.name = name;
        this.slotGroup = slotGroup;
    }

    public ResourceLocation getName() {
        return name;
    }

    public EquipmentSlotGroup getSlotGroup() {
        return slotGroup;
    }
}
